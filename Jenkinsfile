pipeline {
    agent any
    
    tools {
        jdk 'JDK-21'
        maven 'Maven-3.8'
    }
    
    options {
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo '======================================'
                echo '📥 Checking out code from GitHub'
                echo '======================================'
                
                checkout scm
                
                script {
                    env.GIT_COMMIT = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                    env.GIT_BRANCH = sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
                }
                
                echo "Branch: ${env.GIT_BRANCH}"
                echo "Commit: ${env.GIT_COMMIT}"
            }
        }
        
        stage('Environment Check') {
            steps {
                echo '======================================'
                echo '🔧 Verifying Build Environment'
                echo '======================================'
                
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
                echo '======================================'
                echo '🔨 Building Application'
                echo '======================================'
                
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                echo '======================================'
                echo '🧪 Running Tests'
                echo '======================================'
                
                sh 'mvn test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                echo '======================================'
                echo '📦 Creating WAR Package'
                echo '======================================'
                
                sh 'mvn package -DskipTests'
                
                sh '''
                    echo "Build Artifacts:"
                    ls -lh target/*.war
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
                echo '======================================'
                echo '🚀 Deployment Stage'
                echo '======================================'
                
                script {
                    // Check if running on the same server as Tomcat
                    def tomcatDir = '/opt/tomcat/webapps'
                    def warFile = 'target/NumberGuessGame.war'
                    
                    sh """
                        echo "Checking deployment options..."
                        
                        if [ -f "${warFile}" ]; then
                            echo "✅ WAR file built successfully: ${warFile}"
                            echo "   Size: \$(du -h ${warFile} | cut -f1)"
                            echo ""
                            
                            # Check if Tomcat is on the same server
                            if [ -d "${tomcatDir}" ]; then
                                echo "📁 Tomcat directory found locally"
                                echo "   Attempting local deployment..."
                                
                                # Try to copy, but don't fail if no permissions
                                cp ${warFile} ${tomcatDir}/ 2>/dev/null || {
                                    echo "⚠️  Local deployment requires additional permissions"
                                    echo "   WAR file is ready for manual deployment"
                                }
                                
                                # Check if deployment succeeded
                                if [ -f "${tomcatDir}/NumberGuessGame.war" ]; then
                                    echo "✅ Deployment successful!"
                                    echo "   Application URL: http://localhost:8080/NumberGuessGame"
                                else
                                    echo "ℹ️  Manual deployment required"
                                    echo "   Copy ${warFile} to your Tomcat webapps directory"
                                fi
                            else
                                echo "ℹ️  Tomcat not found on this server"
                                echo "   For remote deployment, configure SSH credentials"
                                echo "   WAR file ready at: \$(pwd)/${warFile}"
                            fi
                        else
                            echo "❌ WAR file not found - build may have failed"
                            exit 1
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
                echo '======================================'
                echo '✅ Pipeline Summary'
                echo '======================================'
                
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
        success {
            echo '''
            ====================================
            🎉 BUILD SUCCESSFUL!
            ====================================
            All stages completed successfully.
            WAR file is available in Jenkins artifacts.
            '''
        }
        
        failure {
            echo '''
            ====================================
            ❌ BUILD FAILED
            ====================================
            Check the logs above for error details.
            '''
        }
        
        always {
            echo 'Cleaning up workspace...'
            cleanWs()
        }
    }
}
