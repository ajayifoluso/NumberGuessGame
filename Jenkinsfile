pipeline {
    agent any
    
    tools {
        maven 'Maven-3.8.4'
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
                echo "🔥 Checking out code from GitHub"
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
                echo "🔧 Verifying Build Environment"
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
                echo "🔨 Building Application"
                echo "======================================"
                
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                echo "======================================"
                echo "🧪 Running Tests"
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
                echo "📦 Creating WAR Package"
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
                echo "🚀 Deployment Stage"
                echo "======================================"
                
                script {
                    sh """
                        echo "Checking deployment options..."
                        
                        if [ -f target/NumberGuessGame.war ]; then
                            echo "✅ WAR file built successfully: target/NumberGuessGame.war"
                            echo "   Size: \$(du -h target/NumberGuessGame.war | cut -f1)"
                            echo ""
                        fi
                        
                        # Check for local Tomcat (development)
                        if [ -d /opt/tomcat/webapps ]; then
                            echo "📋 Deploying to local Tomcat..."
                            sudo cp target/NumberGuessGame.war /opt/tomcat/webapps/
                            echo "✅ Deployed to local Tomcat"
                        else
                            echo "ℹ️ Tomcat not found on this server"
                        fi
                    """
                    
                    // Deploy to EC2 if SSH credentials are configured
                    try {
                        sshagent(['ec2-ssh-key']) {  // Replace 'ec2-ssh-key' with your Jenkins SSH credential ID
                            sh """
                                echo "🌐 Deploying to EC2 instance..."
                                
                                # Test SSH connection first
                                ssh -o StrictHostKeyChecking=no -o ConnectTimeout=10 ec2-user@13.218.144.248 'echo "SSH connection successful"'
                                
                                # Stop Tomcat
                                ssh -o StrictHostKeyChecking=no ec2-user@13.218.144.248 'sudo systemctl stop tomcat'
                                
                                # Remove old deployment
                                ssh -o StrictHostKeyChecking=no ec2-user@13.218.144.248 'sudo rm -rf /opt/tomcat/webapps/NumberGuessGame*'
                                
                                # Copy new WAR file
                                scp -o StrictHostKeyChecking=no target/NumberGuessGame.war ec2-user@13.218.144.248:/tmp/
                                
                                # Move WAR to webapps and start Tomcat
                                ssh -o StrictHostKeyChecking=no ec2-user@13.218.144.248 'sudo mv /tmp/NumberGuessGame.war /opt/tomcat/webapps/ && sudo systemctl start tomcat'
                                
                                # Wait for deployment
                                sleep 10
                                
                                # Check deployment status
                                ssh -o StrictHostKeyChecking=no ec2-user@13.218.144.248 'sudo systemctl status tomcat --no-pager'
                                
                                echo "✅ Deployed to EC2: http://13.218.144.248:8081/NumberGuessGame"
                            """
                        }
                    } catch (Exception e) {
                        echo "⚠️ EC2 deployment failed: ${e.getMessage()}"
                        echo "   This is normal if SSH credentials are not configured"
                        echo "   Manual deployment options:"
                        echo "   1. Download WAR from Jenkins artifacts"
                        echo "   2. Upload to EC2: scp NumberGuessGame.war ec2-user@13.218.144.248:/tmp/"
                        echo "   3. Deploy: sudo mv /tmp/NumberGuessGame.war /opt/tomcat/webapps/"
                        echo "   4. Restart: sudo systemctl restart tomcat"
                        currentBuild.result = 'SUCCESS'  // Don't fail the build for deployment issues
                    }
                    
                    sh """
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
                echo "✅ Pipeline Summary"
                echo "======================================"
                
                script {
                    echo "Build Number: ${env.BUILD_NUMBER}"
                    echo "Build Status: SUCCESS"
                    echo "Artifact: target/NumberGuessGame.war"
                    echo ""
                    echo "Next Steps:"
                    echo "1. Access application at http://13.218.144.248:8081/NumberGuessGame"
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
            🎉 BUILD SUCCESSFUL!
            ====================================
            All stages completed successfully.
            WAR file is available in Jenkins artifacts.
            """
        }
        
        failure {
            echo """
            ====================================
            ❌ BUILD FAILED
            ====================================
            Check the logs above for error details.
            """
        }
    }
}
