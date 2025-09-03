pipeline {
    agent any
    
    tools {
        maven 'Maven-3.8'
        jdk 'JDK-21'
    }
    
    environment {
        MAVEN_OPTS = '-Xmx1024m'
        JAVA_OPTS = '-Xms512m -Xmx1024m'
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
        
        stage('Deploy to Tomcat 9 (:8081)') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "🚀 Starting deployment to Tomcat..."
                    
                    // Define variables
                    def tomcatHome = '/home/ec2-user/apache-tomcat-9.0.91'
                    def appName = 'NumberGuessGame'
                    def warFile = "target/${appName}-1.0.0.war"
                    
                    // Check if WAR file exists
                    if (!fileExists(warFile)) {
                        error "WAR file not found: ${warFile}"
                    }
                    
                    // Deploy commands
                    sh """
                        echo "📦 Deploying ${warFile}..."
                        
                        # Stop Tomcat
                        echo "🛑 Stopping Tomcat..."
                        ${tomcatHome}/bin/shutdown.sh || echo "Tomcat was not running"
                        sleep 5
                        
                        # Kill any remaining processes
                        pkill -f tomcat || echo "No Tomcat processes to kill"
                        sleep 2
                        
                        # Clean old deployment
                        echo "🧹 Cleaning old deployment..."
                        rm -rf ${tomcatHome}/webapps/${appName}*
                        rm -rf ${tomcatHome}/work/Catalina/localhost/${appName}
                        
                        # Copy new WAR file
                        echo "📂 Copying WAR file..."
                        cp ${warFile} ${tomcatHome}/webapps/${appName}.war
                        
                        # Start Tomcat
                        echo "▶️ Starting Tomcat..."
                        ${tomcatHome}/bin/startup.sh
                        
                        # Wait for deployment
                        echo "⏳ Waiting for deployment..."
                        sleep 15
                        
                        # Check deployment status
                        if [ -d "${tomcatHome}/webapps/${appName}" ]; then
                            echo "✅ Application deployed successfully!"
                            echo "🌍 Access at: http://\$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):8081/${appName}"
                        else
                            echo "❌ Deployment may have failed"
                            echo "📝 Recent Tomcat logs:"
                            tail -20 ${tomcatHome}/logs/catalina.out
                            exit 1
                        fi
                        
                        # Show final status
                        echo "📊 Final Status:"
                        echo "Tomcat processes:"
                        ps aux | grep tomcat | grep -v grep || echo "No Tomcat processes"
                        echo "Port status:"
                        netstat -tlnp | grep -E ":(8080|8081)" || echo "No services on ports 8080/8081"
                    """
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo '✅ Build OK'
        }
        failure {
            echo '❌ Build Failed'
        }
        unstable {
            echo '⚠️ Build Unstable'
        }
    }
}
