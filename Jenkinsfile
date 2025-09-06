pipeline {
  agent any

  // ====== choose how/where to deploy at build time ======
  parameters {
    choice(name: 'DEPLOY_METHOD', choices: ['ssh','manager','local'],
           description: 'Deploy via SSH copy, Tomcat Manager API, or locally on this host')
    string(name: 'APP_NAME',     defaultValue: 'NumberGuessGame', description: 'WAR/context name')
    string(name: 'DEPLOY_HOST',  defaultValue: '13.218.144.248',  description: 'Target host (ignored for local)')
    string(name: 'DEPLOY_PORT',  defaultValue: '8081',            description: 'Tomcat HTTP port')
    string(name: 'DEPLOY_DIR',   defaultValue: '/opt/tomcat/webapps', description: 'Tomcat webapps dir on target')
    booleanParam(name: 'RUN_SMOKE', defaultValue: true, description: 'Run post-deploy smoke test')
  }

  // ====== use your actual tool names from Manage Jenkins → Tools ======
  tools {
    jdk   'JDK-21'
    maven 'Maven-3.8'
  }

  options { timestamps() }

  // mirror params into env for simpler shell usage
  environment {
    APP_NAME   = "${params.APP_NAME}"
    DEPLOY_HOST= "${params.DEPLOY_HOST}"
    DEPLOY_PORT= "${params.DEPLOY_PORT}"
    DEPLOY_DIR = "${params.DEPLOY_DIR}"
  }

  stages {

    stage('Verify toolchain') {
      steps {
        sh '''
          echo "== Java & Maven versions =="
          java -version
          mvn -v
        '''
      }
    }

    stage('Print Branch') {
      steps {
        script {
          def branch = env.BRANCH_NAME ?: sh(
            returnStdout: true,
            script: 'git name-rev --name-only HEAD 2>/dev/null || git rev-parse --abbrev-ref HEAD 2>/dev/null || echo unknown'
          ).trim()
          echo "🌿 Branch: ${branch}"
        }
      }
    }

    stage('Build & Test') {
      steps {
        sh 'mvn -B -U clean verify'
      }
      post {
        always {
          junit 'target/surefire-reports/*.xml'                        // test results in Jenkins UI
          archiveArtifacts artifacts: 'target/*.war', fingerprint: true // keep WAR for rollback
        }
      }
    }

    // ---- optional SSH reachability check (only when using ssh) ----
    stage('SSH sanity (temp)') {
      when { expression { params.DEPLOY_METHOD == 'ssh' } }
      steps {
        withCredentials([sshUserPrivateKey(credentialsId: 'tomcat-ssh',
                                           keyFileVariable: 'SSH_KEY',
                                           usernameVariable: 'SSH_USER')]) {
          sh '''
            set -euxo pipefail
            ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
              "$SSH_USER@${DEPLOY_HOST}" "whoami; hostname; ls -ld ${DEPLOY_DIR}"
          '''
        }
      }
    }

    // ---- Deploy via SSH (scp to /tmp then sudo move to webapps) ----
    stage('Deploy (ssh)') {
      when { expression { params.DEPLOY_METHOD == 'ssh' } }
      steps {
        withCredentials([sshUserPrivateKey(credentialsId: 'tomcat-ssh',
                                           keyFileVariable: 'SSH_KEY',
                                           usernameVariable: 'SSH_USER')]) {
          sh '''
            set -euxo pipefail
            WAR=$(ls -1 target/*.war | head -n1)
            echo "Deploying $WAR to ${DEPLOY_HOST}:${DEPLOY_DIR}/${APP_NAME}.war via SSH"
            scp -i "$SSH_KEY" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
                "$WAR" "$SSH_USER@${DEPLOY_HOST}:/tmp/${APP_NAME}.war"
            ssh -i "$SSH_KEY" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
                "$SSH_USER@${DEPLOY_HOST}" \
                "sudo mv /tmp/${APP_NAME}.war ${DEPLOY_DIR}/${APP_NAME}.war && sudo chown -R tomcat:tomcat ${DEPLOY_DIR}/${APP_NAME}.war"
          '''
        }
      }
    }

    // ---- Deploy via Tomcat Manager API (no SSH/filesystem perms needed) ----
    stage('Deploy (manager)') {
      when { expression { params.DEPLOY_METHOD == 'manager' } }
      steps {
        withCredentials([usernamePassword(credentialsId: 'tomcat-manager',
                                          usernameVariable: 'TC_USER',
                                          passwordVariable: 'TC_PASS')]) {
          sh '''
            set -euxo pipefail
            WAR=$(ls -1 target/*.war | head -n1)
            APP_URL="http://${DEPLOY_HOST}:${DEPLOY_PORT}/manager/text"
            echo "Undeploying /${APP_NAME} (if exists) via Manager API"
            curl -fsS -u "$TC_USER:$TC_PASS" "$APP_URL/undeploy?path=/${APP_NAME}" || true
            echo "Deploying $WAR to /${APP_NAME} via Manager API"
            curl -fsS -u "$TC_USER:$TC_PASS" -T "$WAR" "$APP_URL/deploy?path=/${APP_NAME}&update=true"
          '''
        }
      }
    }

    // ---- Local deploy (when Jenkins and Tomcat are on the same host) ----
    stage('Deploy (local)') {
      when { expression { params.DEPLOY_METHOD == 'local' } }
      steps {
        sh '''
          set -euxo pipefail
          WAR=$(ls -1 target/*.war | head -n1)
          echo "Copying $WAR to ${DEPLOY_DIR}/${APP_NAME}.war locally"
          sudo cp "$WAR" "${DEPLOY_DIR}/${APP_NAME}.war"
          sudo chown -R tomcat:tomcat "${DEPLOY_DIR}/${APP_NAME}.war" || true
          # If needed: sudo systemctl restart tomcat
        '''
      }
    }

    // ---- Verify app is actually running ----
    stage('Smoke Test') {
      when { expression { return params.RUN_SMOKE } }
      steps {
        sh '''
          set -euxo pipefail
          echo "Waiting for app to come up at http://${DEPLOY_HOST}:${DEPLOY_PORT}/${APP_NAME}/ ..."
          for i in {1..60}; do
            curl -fsS "http://${DEPLOY_HOST}:${DEPLOY_PORT}/${APP_NAME}/" >/dev/null && break || sleep 1
          done
          echo "Hitting the servlet endpoint to validate game response..."
          curl -fsS "http://${DEPLOY_HOST}:${DEPLOY_PORT}/${APP_NAME}/guess?number=50" | grep -E "Correct|Too (low|high)"
        '''
      }
    }
  }

  post {
    always {
      script {
        try { echo 'Cleaning workspace…'; deleteDir() } catch (e) { echo "cleanup skipped: ${e}" }
        try {
          emailext to: 'ajayi.foluso@gmail.com',
                   subject: "Build ${env.JOB_NAME} #${env.BUILD_NUMBER} → ${currentBuild.currentResult}",
                   body: "See ${env.BUILD_URL}"
        } catch (e) { echo "email skipped: ${e}" }
      }
    }
  }
}
