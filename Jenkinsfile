pipeline {
  agent any
  tools {
    jdk   'JDK-21'     // you have this
    maven 'Maven-3.8'  // you have this
  }
  options { timestamps() }

  environment {
    APP_NAME    = 'NumberGuessGame'
    DEPLOY_HOST = '13.218.144.248'      // <-- your server
    DEPLOY_PORT = '8081'
    DEPLOY_DIR  = '/opt/tomcat/webapps' // <-- Tomcat webapps dir on server
  }

  stages {
    stage('Verify toolchain') {
      steps {
        sh '''
          echo "JAVA SHOULD BE 21+ ↓↓↓"
          java -version
          echo "MAVEN + JAVA HOME ↓↓↓"
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
          junit 'target/surefire-reports/*.xml'                  // show test results in Jenkins UI
          archiveArtifacts artifacts: 'target/*.war', fingerprint: true  // keep WAR for rollback
        }
      }
    }

    stage('Deploy to Tomcat (SSH)') {
      when { expression { return env.DEPLOY_HOST?.trim() } }
      steps {
        // Create an SSH credential in Jenkins:
        // Credentials > "SSH Username with private key"
        // ID: tomcat-ssh, Username: e.g., ec2-user (or tomcat), Private key uploaded
        withCredentials([sshUserPrivateKey(credentialsId: 'tomcat-ssh', keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
          sh '''
            set -euxo pipefail
            WAR=$(ls -1 target/*.war | head -n1)
            echo "Deploying $WAR to ${DEPLOY_HOST}:${DEPLOY_DIR}/${APP_NAME}.war"
            scp -i "$SSH_KEY" -o StrictHostKeyChecking=no "$WAR" "$SSH_USER@${DEPLOY_HOST}:${DEPLOY_DIR}/${APP_NAME}.war"
          '''
        }
      }
    }

    stage('Smoke Test') {
      steps {
        sh '''
          set -euxo pipefail
          # Wait up to 60s for Tomcat to expand and start the app
          for i in {1..60}; do
            curl -fsS "http://${DEPLOY_HOST}:${DEPLOY_PORT}/${APP_NAME}/" >/dev/null && break || sleep 1
          done
          # Endpoint should respond with a known string
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
