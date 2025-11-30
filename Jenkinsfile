pipeline {
    agent any

    environment {
        // App identity
        APP_NAME        = "task-manager"
        APP_NAMESPACE   = "${APP_NAME}"

        // Docker image info
        IMAGE_NAME      = "${APP_NAME}-image"
        IMAGE_TAG       = "${BUILD_NUMBER ?: 'local'}"
        FULL_IMAGE      = "${IMAGE_NAME}:${IMAGE_TAG}"

        // Kubernetes config
        K8S_DIR         = "k8s"
        KUBECONFIG      = "C:/Users/dmand/.kube/config"
        CLUSTER_TYPE    = ""                // values: kind | minikube | empty

        // App runtime settings
        APP_PORT        = "8150"            // container port
        NODE_PORT       = "31850"           // NodePort for access
        REPLICA_COUNT   = "2"               // Deployment replicas
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'master', url: 'https://github.com/KaeithEmmanuel/TaskManager'
            }
        }

        stage('Package (Maven)') {
            steps {
                bat "mvn -B -DskipTests package"
            }
        }

        stage('Build Docker Image') {
            steps {
                bat "docker build -t ${FULL_IMAGE} ."
            }
        }
        stages('Run Docker Container (local)') {
            steps {
                script {
                    bat """
                    docker run -d --name ${APP_NAME}-container -p ${NODE_PORT}:${APP_PORT} \\
                        -e APP_PORT=${APP_PORT} \\
                        ${FULL_IMAGE}
                    """
                }
            }
        }

        stage('Run Docker Container (local) - optional') {
            steps {
                script {
                    echo "Skipped local run. Uncomment docker run lines if needed."
                }
            }
        }

        stage('K8s Container Deployment') {
            steps {
                script {
                    withEnv(["KUBECONFIG=${KUBECONFIG}"]) {

                        // Render YAML templates → final manifests
                        bat "envsubst < ${K8S_DIR}/namespace-template.yaml > ${K8S_DIR}/namespace.yaml"
                        bat "envsubst < ${K8S_DIR}/deployment-template.yaml > ${K8S_DIR}/deployment.yaml"
                        bat "envsubst < ${K8S_DIR}/service-template.yaml > ${K8S_DIR}/service.yaml"

                        // Apply manifests
                        bat "kubectl apply -f ${K8S_DIR}/namespace.yaml --validate=false"
                        bat "kubectl apply -f ${K8S_DIR}/deployment.yaml -n ${APP_NAMESPACE} --validate=false"
                        bat "kubectl apply -f ${K8S_DIR}/service.yaml -n ${APP_NAMESPACE} --validate=false"

                        // Force update deployment image
                        bat "kubectl set image deployment/${APP_NAME}-deployment ${APP_NAME}=${FULL_IMAGE} -n ${APP_NAMESPACE} --record"

                        // Wait for rollout success/failure
                        bat "kubectl rollout status deployment/${APP_NAME}-deployment -n ${APP_NAMESPACE} --timeout=120s"
                    }
                }
            }
        }

    }

    post {
        success {
            bat "kubectl -n ${APP_NAMESPACE} get pods"
            echo "✅ Build, Dockerize & K8s Deploy completed successfully!"
        }
        failure {
            bat "kubectl -n ${APP_NAMESPACE} get pods || exit 0"
            echo "❌ Pipeline failed!"
        }
    }
}
