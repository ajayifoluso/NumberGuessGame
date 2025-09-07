pipeline {
    agent any
    
    tools {
        maven 'Maven-3.8'
        jdk 'JDK-21'
    }
    
    options {
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo "======================================"
                echo "Checking out code from GitHub"
                echo "======================================"
                
                checkout scm
                
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: "git rev-parse HEAD",
                        returnStdout: true
                    ).trim()
                    
                    env.GIT_BRANCH = sh(
                        script: "git rev-parse --abbrev-ref HEAD",
                        returnStdout: true
                    ).trim()
                }
                
                echo "Branch: ${env.GIT_BRANCH}"
                echo "Commit: ${env.GIT_COMMIT_SHORT}"
            }
        }
        
        stage('Environment Check') {
            steps {
                echo "======================================"
                echo "Verifying Build Environment"
                echo "======================================"
                
                sh '''
                    echo "Java Version:"
                    java -version
                    echo ""
                    
                    echo "Maven Version:"
                    mvn -version
                    echo ""
                    
                    echo "Current Directory:"
                    pwd
                    echo ""
                    
                    echo "Project Files:"
                    ls -la
                '''
            }
        }
        
        stage('Build') {
            steps {
                echo "======================================"
                echo "Building Application"
                echo "======================================"
                
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                echo "======================================"
                echo "Running Tests"
                echo "======================================"
                
                sh 'mvn test'
            }
            
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                echo "======================================"
                echo "Creating WAR Package"
                echo "======================================"
                
                sh 'mvn package -DskipTests'
                
                sh '''
                    echo "Build Artifacts:"
                    ls -lh target/NumberGuessGame.war
                    echo ""
                    
                    echo "WAR file details:"
                    file target/NumberGuessGame.war
                '''
            }
            
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.war', fingerprint: true
                }
            }
        }
        
        stage('Deploy') {
            steps {
                echo "======================================"
                echo "Deployment Stage"
                echo "======================================"
                
                script {
                    sh """
                        echo "Checking deployment options..."
                        
                        if [ -f target/NumberGuessGame.war ]; then
                            echo "WAR file built successfully: target/NumberGuessGame.war"
                            echo "   Size: \$(du -h target/NumberGuessGame.war | cut -f1)"
                            echo ""
                        fi
                        
                        # Check for local Tomcat (development)
                        if [ -d /opt/tomcat/webapps ]; then
                            echo "Deploying to local Tomcat..."
                            sudo cp target/NumberGuessGame.war /opt/tomcat/webapps/
                            echo "Deployed to local Tomcat"
                        else
                            echo "Tomcat not found on this server"
                            echo "   For remote deployment, configure SSH credentials"
                            echo "   WAR file ready at: \$(pwd)/target/NumberGuessGame.war"
                        fi
                        
                        echo ""
                        echo "======================================"
                        echo "Deployment stage completed"
                        echo "======================================"
                    """
                }
            }
        }
        
        stage('Verify') {
            steps {
                echo "======================================"
                echo "Pipeline Summary"
                echo "======================================"
                
                script {
                    echo "Build Number: ${env.BUILD_NUMBER}"
                    echo "Build Status: SUCCESS"
                    echo "Artifact: target/NumberGuessGame.war"
                    echo ""
                    echo "Next Steps:"
                    echo "1. Access application at http://your-server:8080/NumberGuessGame"
                    echo "2. Or download the WAR file from Jenkins artifacts"
                    echo "3. Deploy manually to any Servlet container"
                }
            }
        }
    }
    
    post {
        always {
            echo "Cleaning up workspace..."
            cleanWs()
        }
        
        success {
            echo """
            ====================================
            BUILD SUCCESSFUL!
            ====================================
            All stages completed successfully.
            WAR file is available in Jenkins artifacts.
            """
        }
        
        failure {
            echo """
            ====================================
            BUILD FAILED
            ====================================
            Check the logs above for error details.
            """
        }
    }
}
