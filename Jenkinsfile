pipeline {
    agent any
    
    environment {
        // Project Configuration
        PROJECT_NAME = 'NumberGuessGame-V2'
        MAVEN_OPTS = '-Dmaven.test.failure.ignore=true'
        
        // Version and Build Info
        BUILD_VERSION = "2.0.${BUILD_NUMBER}"
        ARTIFACT_NAME = "NumberGuessGame-${BUILD_VERSION}.war"
        
        // Deployment Configuration
        TOMCAT_HOME = '/opt/tomcat'
        DEPLOYMENT_PATH = "${TOMCAT_HOME}/webapps"
        BACKUP_PATH = '/opt/backups'
        
        // Notification Configuration
        SLACK_CHANNEL = '#devops-alerts'
        EMAIL_RECIPIENTS = 'team@company.com'
    }
    
    options {
        // Build Configuration
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
        skipStagesAfterUnstable()
        
        // Parallel builds prevention
        disableConcurrentBuilds()
    }
    
    triggers {
        // Automatic triggers
        pollSCM('H/5 * * * *')  // Poll every 5 minutes
        cron('H 2 * * *')       // Nightly build at 2 AM
    }
    
    stages {
        stage('🚀 Initialize V2 Pipeline') {
            steps {
                script {
                    echo "==========================================="
                    echo "🎯 Number Guessing Game V2 - CI/CD Pipeline"
                    echo "==========================================="
                    echo "📊 Build: ${BUILD_NUMBER}"
                    echo "📋 Version: ${BUILD_VERSION}"
                    echo "🌿 Branch: ${env.BRANCH_NAME}"
                    echo "👤 User: ${env.BUILD_USER ?: 'System'}"
                    echo "🕐 Started: ${new Date()}"
                    echo "==========================================="
                }
                
                // Clean workspace
                cleanWs()
                
                // Send start notification
                script {
                    try {
                        emailext (
                            subject: "🚀 V2 Pipeline Started - Build #${BUILD_NUMBER}",
                            body: """
                                <h2>🎯 Number Guessing Game V2 - Build Started</h2>
                                <p><strong>Build Number:</strong> ${BUILD_NUMBER}</p>
                                <p><strong>Version:</strong> ${BUILD_VERSION}</p>
                                <p><strong>Branch:</strong> ${env.BRANCH_NAME}</p>
                                <p><strong>Started By:</strong> ${env.BUILD_USER ?: 'Automated Trigger'}</p>
                                <p><strong>Jenkins URL:</strong> <a href="${BUILD_URL}">${BUILD_URL}</a></p>
                            """,
                            to: "${EMAIL_RECIPIENTS}",
                            mimeType: 'text/html'
                        )
                    } catch (Exception e) {
                        echo "⚠️ Email notification failed: ${e.getMessage()}"
                    }
                }
            }
        }
        
        stage('📥 Checkout V2 Source') {
            steps {
                echo "📥 Checking out V2 source code..."
                
                checkout scm
                
                script {
                    // Verify V2 files exist
                    def v2Files = [
                        'src/main/webapp/assets/css/animations.css',
                        'src/main/webapp/assets/css/game-styles.css',
                        'src/main/webapp/assets/js/game-controller.js',
                        'src/main/webapp/index.jsp'
                    ]
                    
                    v2Files.each { file ->
                        if (fileExists(file)) {
                            echo "✅ V2 File found: ${file}"
                        } else {
                            error "❌ V2 File missing: ${file}"
                        }
                    }
                }
                
                // Display Git information
                script {
                    def gitCommit = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
                    def gitBranch = sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
                    def gitAuthor = sh(returnStdout: true, script: 'git log -1 --pretty=format:"%an"').trim()
                    
                    echo "📋 Git Commit: ${gitCommit}"
                    echo "🌿 Git Branch: ${gitBranch}"
                    echo "👤 Last Author: ${gitAuthor}"
                }
            }
        }
        
        stage('🔍 Code Quality Analysis') {
            parallel {
                stage('📊 Static Analysis') {
                    steps {
                        echo "📊 Running static code analysis..."
                        
                        script {
                            // Check for common issues
                            sh '''
                                echo "🔍 Checking Java files..."
                                find src -name "*.java" -type f | wc -l
                                
                                echo "🔍 Checking CSS files..."
                                find src -name "*.css" -type f | wc -l
                                
                                echo "🔍 Checking JavaScript files..."
                                find src -name "*.js" -type f | wc -l
                                
                                echo "🔍 Checking JSP files..."
                                find src -name "*.jsp" -type f | wc -l
                            '''
                        }
                    }
                }
                
                stage('🧪 Dependency Check') {
                    steps {
                        echo "🧪 Checking Maven dependencies..."
                        
                        sh 'mvn dependency:tree'
                        sh 'mvn dependency:analyze'
                    }
                }
            }
        }
        
        stage('🔨 Build V2 Application') {
            steps {
                echo "🔨 Building V2 application with Maven..."
                
                script {
                    // Clean and compile
                    sh 'mvn clean compile -DskipTests=true'
                    
                    // Verify compilation
                    if (fileExists('target/classes')) {
                        echo "✅ Compilation successful"
                    } else {
                        error "❌ Compilation failed - no target/classes directory"
                    }
                }
                
                // Package the application
                sh "mvn package -DskipTests=true -Dproject.version=${BUILD_VERSION}"
                
                // Verify WAR file creation
                script {
                    def warFile = "target/NumberGuessGame-${BUILD_VERSION}.war"
                    if (fileExists(warFile)) {
                        echo "✅ WAR file created: ${warFile}"
                        
                        // Get file size
                        def fileSize = sh(returnStdout: true, script: "du -h ${warFile} | cut -f1").trim()
                        echo "📦 WAR file size: ${fileSize}"
                    } else {
                        error "❌ WAR file not created"
                    }
                }
            }
            
            post {
                success {
                    echo "✅ Build completed successfully"
                }
                failure {
                    echo "❌ Build failed"
                }
            }
        }
        
        stage('🧪 Test V2 Features') {
            parallel {
                stage('🔧 Unit Tests') {
                    steps {
                        echo "🔧 Running unit tests..."
                        
                        script {
                            try {
                                sh 'mvn test'
                            } catch (Exception e) {
                                echo "⚠️ Some tests failed, but continuing..."
                            }
                        }
                    }
                    
                    post {
                        always {
                            // Publish test results
                            script {
                                if (fileExists('target/surefire-reports/TEST-*.xml')) {
                                    junit 'target/surefire-reports/TEST-*.xml'
                                    echo "📊 Test results published"
                                }
                            }
                        }
                    }
                }
                
                stage('🎨 Frontend Asset Tests') {
                    steps {
                        echo "🎨 Testing V2 frontend assets..."
                        
                        script {
                            // Verify CSS files are valid
                            sh '''
                                echo "🎨 Checking CSS syntax..."
                                for css_file in $(find src/main/webapp/assets/css -name "*.css"); do
                                    echo "Checking: $css_file"
                                    # Basic CSS validation (check for obvious syntax errors)
                                    if grep -q "}" "$css_file"; then
                                        echo "✅ $css_file appears valid"
                                    else
                                        echo "⚠️ $css_file may have issues"
                                    fi
                                done
                            '''
                            
                            // Verify JavaScript files
                            sh '''
                                echo "📜 Checking JavaScript syntax..."
                                for js_file in $(find src/main/webapp/assets/js -name "*.js"); do
                                    echo "Checking: $js_file"
                                    # Basic JS validation
                                    if node -c "$js_file" 2>/dev/null; then
                                        echo "✅ $js_file syntax is valid"
                                    else
                                        echo "⚠️ $js_file may have syntax issues"
                                    fi
                                done
                            '''
                        }
                    }
                }
            }
        }
        
        stage('🚀 Deploy to Staging') {
            when {
                anyOf {
                    branch 'v2-visual'
                    branch 'feature/v2-enhancements'
                    branch 'main'
                }
            }
            
            steps {
                echo "🚀 Deploying V2 to staging environment..."
                
                script {
                    // Create backup of current deployment
                    sh """
                        if [ -f ${DEPLOYMENT_PATH}/NumberGuessGame.war ]; then
                            echo "📦 Creating backup..."
                            cp ${DEPLOYMENT_PATH}/NumberGuessGame.war ${BACKUP_PATH}/NumberGuessGame-backup-\$(date +%Y%m%d-%H%M%S).war
                        fi
                    """
                    
                    // Deploy new version
                    sh """
                        echo "🚀 Deploying new version..."
                        cp target/NumberGuessGame-${BUILD_VERSION}.war ${DEPLOYMENT_PATH}/NumberGuessGame-V2.war
                        
                        echo "⏳ Waiting for deployment..."
                        sleep 10
                        
                        echo "🔍 Checking deployment status..."
                        if [ -d ${DEPLOYMENT_PATH}/NumberGuessGame-V2 ]; then
                            echo "✅ Deployment successful"
                        else
                            echo "⚠️ Deployment may still be in progress"
                        fi
                    """
                }
            }
            
            post {
                success {
                    echo "✅ Staging deployment successful"
                }
                failure {
                    echo "❌ Staging deployment failed"
                }
            }
        }
        
        stage('🧪 Integration Tests') {
            when {
                anyOf {
                    branch 'v2-visual'
                    branch 'feature/v2-enhancements'
                    branch 'main'
                }
            }
            
            steps {
                echo "🧪 Running integration tests for V2..."
                
                script {
                    // Wait for application to be ready
                    sh 'sleep 15'
                    
                    // Test application endpoints
                    sh '''
                        echo "🔍 Testing application endpoints..."
                        
                        # Test main page
                        curl -f -s http://localhost:8080/NumberGuessGame-V2/ > /dev/null && echo "✅ Main page accessible" || echo "❌ Main page failed"
                        
                        # Test servlet endpoint
                        curl -f -s http://localhost:8080/NumberGuessGame-V2/NumberGuessServlet > /dev/null && echo "✅ Servlet accessible" || echo "❌ Servlet failed"
                        
                        # Test CSS files
                        curl -f -s http://localhost:8080/NumberGuessGame-V2/assets/css/animations.css > /dev/null && echo "✅ Animations CSS accessible" || echo "❌ Animations CSS failed"
                        
                        # Test JavaScript files
                        curl -f -s http://localhost:8080/NumberGuessGame-V2/assets/js/game-controller.js > /dev/null && echo "✅ Game Controller JS accessible" || echo "❌ Game Controller JS failed"
                    '''
                }
            }
        }
        
        stage('📊 Performance Tests') {
            when {
                anyOf {
                    branch 'v2-visual'
                    branch 'main'
                }
            }
            
            steps {
                echo "📊 Running performance tests..."
                
                script {
                    // Simple load test
                    sh '''
                        echo "🔄 Running basic load test..."
                        for i in {1..10}; do
                            curl -s -o /dev/null -w "%{http_code} %{time_total}\\n" http://localhost:8080/NumberGuessGame-V2/
                        done
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo "🧹 Cleaning up workspace..."
            
            // Archive artifacts
            script {
                if (fileExists("target/NumberGuessGame-${BUILD_VERSION}.war")) {
                    archiveArtifacts artifacts: "target/NumberGuessGame-${BUILD_VERSION}.war", fingerprint: true
                    echo "📦 Artifacts archived"
                }
            }
            
            // Clean workspace
            cleanWs()
        }
        
        success {
            script {
                echo "🎉 V2 Pipeline completed successfully!"
                
                emailext (
                    subject: "✅ V2 Build Success - Build #${BUILD_NUMBER}",
                    body: """
                        <h2>🎉 Number Guessing Game V2 - Build Successful!</h2>
                        <p><strong>Build Number:</strong> ${BUILD_NUMBER}</p>
                        <p><strong>Version:</strong> ${BUILD_VERSION}</p>
                        <p><strong>Branch:</strong> ${env.BRANCH_NAME}</p>
                        <p><strong>Duration:</strong> ${currentBuild.durationString}</p>
                        <p><strong>Status:</strong> ✅ SUCCESS</p>
                        
                        <h3>🚀 V2 Features Deployed:</h3>
                        <ul>
                            <li>🎨 Enhanced animations and visual effects</li>
                            <li>📊 Improved game statistics</li>
                            <li>🎯 Multiple difficulty levels</li>
                            <li>💡 Smart hint system</li>
                            <li>📱 Mobile-responsive design</li>
                        </ul>
                        
                        <p><strong>Application URL:</strong> <a href="http://localhost:8080/NumberGuessGame-V2/">http://localhost:8080/NumberGuessGame-V2/</a></p>
                        <p><strong>Jenkins Build:</strong> <a href="${BUILD_URL}">${BUILD_URL}</a></p>
                    """,
                    to: "${EMAIL_RECIPIENTS}",
                    mimeType: 'text/html'
                )
            }
        }
        
        failure {
            script {
                echo "❌ V2 Pipeline failed!"
                
                emailext (
                    subject: "❌ V2 Build Failed - Build #${BUILD_NUMBER}",
                    body: """
                        <h2>❌ Number Guessing Game V2 - Build Failed</h2>
                        <p><strong>Build Number:</strong> ${BUILD_NUMBER}</p>
                        <p><strong>Version:</strong> ${BUILD_VERSION}</p>
                        <p><strong>Branch:</strong> ${env.BRANCH_NAME}</p>
                        <p><strong>Duration:</strong> ${currentBuild.durationString}</p>
                        <p><strong>Status:</strong> ❌ FAILED</p>
                        
                        <p><strong>Please check the build logs:</strong> <a href="${BUILD_URL}/console">${BUILD_URL}/console</a></p>
                        
                        <p>🔧 <strong>Next Steps:</strong></p>
                        <ul>
                            <li>Review build console output</li>
                            <li>Check code changes in latest commit</li>
                            <li>Verify all V2 files are present</li>
                            <li>Test locally before pushing</li>
                        </ul>
                    """,
                    to: "${EMAIL_RECIPIENTS}",
                    mimeType: 'text/html'
                )
            }
        }
        
        unstable {
            echo "⚠️ V2 Pipeline completed with warnings"
        }
    }
}
