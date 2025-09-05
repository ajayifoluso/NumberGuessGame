pipeline {
  agent any
  tools {
    jdk 'JDK-21'          // Make sure this tool exists in Manage Jenkins → Tools
    maven 'Maven-3.9'     // Optional: define a Maven tool too
  }
  options { timestamps() }

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
    }
  }

  post {
    always {
      script {
        try { deleteDir() } catch (e) { echo "cleanup skipped: ${e}" }
        try {
          emailext to: 'ajayi.foluso@gmail.com',
                   subject: "Build ${env.JOB_NAME} #${env.BUILD_NUMBER} → ${currentBuild.currentResult}",
                   body: "See ${env.BUILD_URL}"
        } catch (e) { echo "email skipped: ${e}" }
      }
    }
  }
}
