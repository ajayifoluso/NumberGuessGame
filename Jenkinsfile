pipeline {
  agent any

  tools {
    maven 'maven'   // must match your Jenkins tool name
    jdk   'jdk17'   // must match your Jenkins tool name
  }

  options { timestamps() }

  stages {
    stage('Env') {
      steps {
        sh '''
          java -version
          mvn -v
          ls -la
          test -f pom.xml || (echo "pom.xml NOT found at repo root" && exit 2)
        '''
      }
    }

    stage('Build') {
      steps {
        sh 'mvn -B -e -U clean package'
      }
    }

    stage('Archive') {
      steps {
        archiveArtifacts artifacts: 'target/**/*', allowEmptyArchive: true
        junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
      }
    }
  }

  post {
    success { echo '✅ Build OK' }
    failure { echo '❌ Build failed — scroll up to the first [ERROR] from Maven.' }
  }
}
