pipeline {
    agent any
    
    environment {
        MAVEN_OPTS = '-Xmx1024m'
    }
    
    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        skipDefaultCheckout(false)
    }
    
    stages {
        stage('Env') {
            steps {
                sh 'java -version'
                sh 'mvn -v'
                sh 'ls -la'
                sh 'test -f pom.xml'
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn -B -e -U clean package'
            }
        }
        
        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'target/*.war', fingerprint: true
                junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: false
            }
        }
        
        stage('Deploy to Tomcat') {
            when {
                branch 'main'
            }
            steps {
                script {
                    def tomcatHome = '/home/ec2-user/apache-tomcat-9.0.91'
                    def appName = 'NumberGuessGame'
                    def warFile = "target/${appName}-1.0.0.war"
                    
                    if (!fileExists(warFile)) {
                        error "WAR file not found: ${warFile}"
                    }
                    
                    sh """
                        echo "Stopping Tomcat..."
                        ${tomcatHome}/bin/shutdown.sh || true
                        sleep 5
                        
                        echo "Cleaning old deployment..."
                        rm -rf ${tomcatHome}/webapps/${appName}*
                        rm -rf ${tomcatHome}/work/Catalina/localhost/${appName}
                        
                        echo "Deploying new WAR..."
                        cp ${warFile} ${tomcatHome}/webapps/${appName}.war
                        
                        echo "Starting Tomcat..."
                        ${tomcatHome}/bin/startup.sh
                        
                        echo "Waiting for deployment..."
                        sleep 15
                        
                        if [ -d "${tomcatHome}/webapps/${appName}" ]; then
                            echo "Deployment successful"
                        else
                            echo "Deployment failed"
                            tail -20 ${tomcatHome}/logs/catalina.out
                            exit 1
                        fi
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo 'Build completed successfully'
        }
        failure {
            echo 'Build failed'
        }
    }
}
