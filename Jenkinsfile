pipeline {
  agent any

  tools {
    maven 'Maven-3.8'   // must match Manage Jenkins → Tools
    jdk   'JDK-21'      // must match Manage Jenkins → Tools
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
stage('Deploy to Tomcat 9 (:8081)') {
  when { branch 'main' }
  steps {
    sh '''
      set -e
      TOMCAT_HOME="/home/ec2-user/apache-tomcat-9.0.91"
      WAR="target/NumberGuessGame-1.0.0.war"
      test -f "$WAR" || { echo "WAR not found: $WAR"; exit 2; }

      cp "$WAR" "$TOMCAT_HOME/webapps/NumberGuessGame.war"

      "$TOMCAT_HOME/bin/shutdown.sh" || true
      sleep 3
      "$TOMCAT_HOME/bin/startup.sh"

      echo "Waiting for Tomcat..."
      sleep 5
      curl -fsS http://localhost:8081/NumberGuessGame/ >/dev/null || {
        echo "Health check failed at /NumberGuessGame/"; exit 1;
      }
      echo "✅ Deployed and healthy on :8081"
    '''
  }
}

