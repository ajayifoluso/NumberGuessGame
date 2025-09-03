pipeline {
  agent any
  tools {
    jdk 'JDK-21'       // your defined JDK tool
    // (omit Maven tool if you're using system mvn)
  }
  environment {
    // Force mvn to use Java 21
    JAVA_HOME = '/usr/lib/jvm/java-21-amazon-corretto.x86_64'
    PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
  }
  options { timestamps() }

  stages {
    stage('Env') {
      steps {
        sh '''
          echo "== Java & Maven =="
          java -version
          mvn -v
        '''
      }
    }
    stage('Build') {
      steps {
        sh 'mvn -B -e -U clean package'
      }
    }
    // Archive/Deploy stages...
  }
}
