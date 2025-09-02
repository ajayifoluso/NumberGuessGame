pipeline {
    agent any
    
    tools {
        maven 'Maven-3.8'
        jdk 'JDK-21'
    }
    
    environment {
        TOMCAT_HOME = '/opt/tomcat'
        APP_NAME = 'NumberGuessGame'
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code from GitHub...'
                checkout scm
                echo "Repository: https://github.com/ajayifoluso/NumberGuessGame.git"
            }
        }
        
        stage('Build') {
            steps {
                echo 'Building the application with Java 21...'
                sh 'java -version'
                sh 'mvn -version'
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                echo 'Running JUnit tests...'
                sh 'mvn test'
            }
            post {
                always {
                    publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/surefire-reports/*', fingerprint: true, allowEmptyArchive: true
                }
            }
        }
        
        stage('Package') {
            steps {
                echo 'Packaging the application into WAR file...'
                sh 'mvn package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.war', fingerprint: true
                    echo "WAR file created: target/NumberGuessGame-1.0-SNAPSHOT.war"
                }
            }
        }
        
        stage('Deploy to Tomcat') {
            steps {
                script {
                    echo 'Deploying application to Tomcat...'
                    sh '''
                        # Stop Tomcat if running
                        ${TOMCAT_HOME}/bin/shutdown.sh || true
                        sleep 5
                        
                        # Remove old deployment
                        rm -rf ${TOMCAT_HOME}/webapps/${APP_NAME}*
                        
                        # Copy new WAR file
                        cp target/*.war ${TOMCAT_HOME}/webapps/${APP_NAME}.war
                        
                        # Start Tomcat
                        ${TOMCAT_HOME}/bin/startup.sh
                        
                        echo "Deployment completed!"
                    '''
                }
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    echo 'Performing application health check...'
                    sh '''
                        echo "Waiting for application to start..."
                        sleep 30
                        
                        for i in {1..10}; do
                            echo "Health check attempt $i/10..."
                            if curl -f -s http://localhost:8081/${APP_NAME}/; then
                                echo "✅ Application is running successfully!"
                                break
                            else
                                echo "⏳ Attempt $i: waiting 10 seconds..."
                                sleep 10
                            fi
                        done
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline execution completed.'
            cleanWs()
        }
        success {
            echo '🎉 Pipeline succeeded!'
            echo "🌐 Application URL: http://localhost:8081/${APP_NAME}"
        }
        failure {
            echo '❌ Pipeline failed. Check logs for details.'
        }
    }
}
