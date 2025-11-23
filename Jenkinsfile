pipeline {
    agent any
    
    // Variáveis de ambiente para configuração
    environment {
        // Docker
        DOCKER_IMAGE_NAME = 'ecommerce'
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT.take(7)}"
        
        // Aplicação
        APP_NAME = 'ecommerce'
        APP_VERSION = "${env.BUILD_NUMBER}"
        APP_PORT = '8081'
    }
    
    // Opções do pipeline
    options {
        timestamps() // Adiciona timestamp em todas as mensagens
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    
    stages {
        // Stage 1: Checkout
        stage('Checkout') {
            steps {
                script {
                    def startTime = System.currentTimeMillis()
                    echo "🚀 Iniciando pipeline de deploy - Build #${env.BUILD_NUMBER}"
                    
                    checkout scm
                    
                    def duration = System.currentTimeMillis() - startTime
                    echo "✅ Checkout concluído em ${duration}ms (${duration/1000}s)"
                    env.CHECKOUT_TIME = duration
                }
            }
        }
        
        // Stage 2: Build e Testes
        stage('Build & Test') {
            steps {
                script {
                    def startTime = System.currentTimeMillis()
                    echo "🔨 Iniciando build e testes..."
                    
                    sh '''
                        mvn clean package -DskipTests=false
                    '''
                    
                    def duration = System.currentTimeMillis() - startTime
                    echo "✅ Build e testes concluídos em ${duration}ms (${duration/1000}s)"
                    env.BUILD_TIME = duration
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }
        
        // Stage 3: Construir Imagem Docker
        stage('Build Docker Image') {
            steps {
                script {
                    def startTime = System.currentTimeMillis()
                    echo "🐳 Construindo imagem Docker..."
                    
                    docker.build("${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}")
                    docker.build("${DOCKER_IMAGE_NAME}:latest")
                    
                    def duration = System.currentTimeMillis() - startTime
                    echo "✅ Imagem Docker construída em ${duration}ms (${duration/1000}s)"
                    env.DOCKER_BUILD_TIME = duration
                }
            }
        }
        
        // Stage 4: Push para Registry (opcional)
        stage('Push Docker Image') {
            when {
                anyOf {
                    branch 'main'
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                script {
                    def startTime = System.currentTimeMillis()
                    echo "📤 Fazendo push da imagem para registry..."
                    
                    // Tentar usar credencial, se não existir, pular push
                    try {
                        def registry = credentials('docker-registry-url')
                        docker.withRegistry("https://${registry}") {
                            docker.image("${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}").push()
                            docker.image("${DOCKER_IMAGE_NAME}:latest").push()
                        }
                        def duration = System.currentTimeMillis() - startTime
                        echo "✅ Push concluído em ${duration}ms (${duration/1000}s)"
                        env.DOCKER_PUSH_TIME = duration
                    } catch (Exception e) {
                        echo "⚠️ Credencial docker-registry-url não configurada. Pulando push."
                        env.DOCKER_PUSH_TIME = 0
                    }
                }
            }
        }
        
        // Stage 5: Deploy (Docker Compose)
        stage('Deploy - Docker Compose') {
            steps {
                script {
                    def startTime = System.currentTimeMillis()
                    echo "🚀 Iniciando deploy com Docker Compose..."
                    
                    // Criar docker-compose.yml se não existir
                    writeFile file: 'docker-compose.yml', text: """
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: ecommerce-postgres
    environment:
      POSTGRES_DB: ecommerce
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: \${POSTGRES_PASSWORD:-postgres}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - ecommerce-network

  app:
    image: ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}
    container_name: ecommerce-app
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      DATABASE_URL: jdbc:postgresql://postgres:5432/ecommerce
      DATABASE_USERNAME: postgres
      DATABASE_PASSWORD: \${POSTGRES_PASSWORD:-postgres}
      JWT_SECRET: \${JWT_SECRET:-your-secret-key-change-this-in-production-min-256-bits}
      ADMIN_KEY: \${ADMIN_KEY:-}
      SERVER_PORT: 8081
    ports:
      - "8081:8081"
    networks:
      - ecommerce-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:8081/actuator/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

volumes:
  postgres_data:

networks:
  ecommerce-network:
    driver: bridge
"""
                    
                    // Parar containers antigos
                    sh '''
                        docker-compose down || true
                    '''
                    
                    // Iniciar novos containers
                    sh '''
                        docker-compose up -d
                    '''
                    
                    // Aguardar aplicação ficar pronta
                    sh """
                        echo "⏳ Aguardando aplicação ficar pronta..."
                        timeout 120 bash -c 'until curl -f http://localhost:${APP_PORT}/actuator/health 2>/dev/null; do sleep 2; done' || exit 1
                        echo "✅ Aplicação está pronta!"
                    """
                    
                    def duration = System.currentTimeMillis() - startTime
                    echo "✅ Deploy concluído em ${duration}ms (${duration/1000}s)"
                    env.DEPLOY_TIME = duration
                }
            }
        }
        
        // Stage 6: Health Check
        stage('Health Check') {
            steps {
                script {
                    def startTime = System.currentTimeMillis()
                    echo "🏥 Verificando saúde da aplicação..."
                    
                    sh """
                        curl -f http://localhost:${APP_PORT}/actuator/health || exit 1
                        echo "✅ Aplicação está saudável!"
                    """
                    
                    def duration = System.currentTimeMillis() - startTime
                    echo "✅ Health check concluído em ${duration}ms (${duration/1000}s)"
                    env.HEALTH_CHECK_TIME = duration
                }
            }
        }
    }
    
    post {
        always {
            script {
                // Calcular tempo total de deploy
                def totalTime = (
                    (env.CHECKOUT_TIME ?: '0').toLong() +
                    (env.BUILD_TIME ?: '0').toLong() +
                    (env.DOCKER_BUILD_TIME ?: '0').toLong() +
                    (env.DOCKER_PUSH_TIME ?: '0').toLong() +
                    (env.DEPLOY_TIME ?: '0').toLong() +
                    (env.HEALTH_CHECK_TIME ?: '0').toLong()
                )
                
                // Publicar métricas de tempo
                echo """
╔══════════════════════════════════════════════════════════╗
║          📊 MÉTRICAS DE TEMPO DE DEPLOY                  ║
╠══════════════════════════════════════════════════════════╣
║ Checkout:        ${String.format('%8.2f', (env.CHECKOUT_TIME ?: '0').toLong() / 1000.0)}s
║ Build & Test:    ${String.format('%8.2f', (env.BUILD_TIME ?: '0').toLong() / 1000.0)}s
║ Docker Build:    ${String.format('%8.2f', (env.DOCKER_BUILD_TIME ?: '0').toLong() / 1000.0)}s
║ Docker Push:     ${String.format('%8.2f', (env.DOCKER_PUSH_TIME ?: '0').toLong() / 1000.0)}s
║ Deploy:          ${String.format('%8.2f', (env.DEPLOY_TIME ?: '0').toLong() / 1000.0)}s
║ Health Check:    ${String.format('%8.2f', (env.HEALTH_CHECK_TIME ?: '0').toLong() / 1000.0)}s
╠══════════════════════════════════════════════════════════╣
║ ⏱️  TEMPO TOTAL: ${String.format('%8.2f', totalTime / 1000.0)}s
╚══════════════════════════════════════════════════════════╝
"""
                
                // Salvar métricas em arquivo para análise
                def timestamp = sh(script: 'date -u +"%Y-%m-%dT%H:%M:%SZ"', returnStdout: true).trim()
                writeFile file: 'deploy-metrics.json', text: """
{
  "buildNumber": "${env.BUILD_NUMBER}",
  "timestamp": "${timestamp}",
  "gitCommit": "${env.GIT_COMMIT ?: 'N/A'}",
  "branch": "${env.BRANCH_NAME ?: 'N/A'}",
  "metrics": {
    "checkout": ${env.CHECKOUT_TIME ?: 0},
    "build": ${env.BUILD_TIME ?: 0},
    "dockerBuild": ${env.DOCKER_BUILD_TIME ?: 0},
    "dockerPush": ${env.DOCKER_PUSH_TIME ?: 0},
    "deploy": ${env.DEPLOY_TIME ?: 0},
    "healthCheck": ${env.HEALTH_CHECK_TIME ?: 0},
    "total": ${totalTime}
  }
}
"""
                
                archiveArtifacts artifacts: 'deploy-metrics.json', fingerprint: true
            }
        }
        success {
            echo "✅ Pipeline executado com sucesso!"
        }
        failure {
            echo "❌ Pipeline falhou!"
        }
        cleanup {
            // Limpar workspace se necessário (apenas se houver contexto)
            script {
                try {
                    cleanWs()
                } catch (Exception e) {
                    echo "⚠️ Não foi possível limpar workspace: ${e.message}"
                }
            }
        }
    }
}
