pipeline {
  agent any

  environment {
    IMAGE_TAG = "${env.BUILD_NUMBER ?: 'local'}"
    IMAGE = "task-manager:${IMAGE_TAG}"
    KUBE_NAMESPACE = "task-manager"
    K8S_DIR = "k8s"
    KUBECONFIG_FILE = ""C:/Users/dmand/.kube/config"
    CLUSTER_TYPE = ""
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build (Maven)') {
      steps {
        powershell "mvn -B clean package -DskipTests"
      }
    }

    stage('Build Docker image') {
      steps {
        powershell "docker build -t ${IMAGE} ."
      }
    }

    stage('Deploy to Kubernetes') {
      steps {
        script {
          if (env.KUBECONFIG_FILE?.trim()) {
            powershell "setx KUBECONFIG ${KUBECONFIG_FILE}"
          }

          if (env.CLUSTER_TYPE == 'kind') {
            powershell "kind load docker-image ${IMAGE}"
          }
          else if (env.CLUSTER_TYPE == 'minikube') {
            powershell "minikube image load ${IMAGE}"
          }

          powershell "kubectl apply -f ${K8S_DIR}/namespace.yaml"
          powershell "kubectl apply -f ${K8S_DIR}/deployment.yaml -n ${KUBE_NAMESPACE}"
          powershell "kubectl apply -f ${K8S_DIR}/service.yaml -n ${KUBE_NAMESPACE}"
          powershell "kubectl set image deployment/task-manager-deployment -n ${KUBE_NAMESPACE} task-manager=${IMAGE} --record"
          powershell "kubectl rollout status deployment/task-manager-deployment -n ${KUBE_NAMESPACE} --timeout=120s"
        }
      }
    }

    stage('Smoke Test') {
      steps {
        powershell """
          kubectl -n ${KUBE_NAMESPACE} port-forward svc/task-manager 18080:8150 > NUL 2>&1 &
          Start-Sleep -Seconds 2
          \$status = (Invoke-WebRequest -Uri http://localhost:18080/api/tasks -UseBasicParsing).StatusCode
          if (\$status -eq 200 -or \$status -eq 204) { exit 0 } else { exit 1 }
        """
      }
    }
  }

  post {
    success {
      powershell "kubectl -n ${KUBE_NAMESPACE} get pods"
    }
    failure {
      powershell "kubectl -n ${KUBE_NAMESPACE} get pods"
    }
  }
}
