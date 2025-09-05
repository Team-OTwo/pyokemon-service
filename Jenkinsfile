// 오케스트레이터 Jenkinsfile

pipeline {
    agent any

    stages {
        stage('Detect Changes and Trigger Builds') {
            steps {
                script {
                    checkout scm;

                    // 관리할 전체 서비스 목록
                    def allServices = ['account', 'booking', 'event', 'did', 'notification', 'payment']

                    // 변경된 파일 목록을 가져옴
                    def changedFiles = getChangedFiles()
                    echo "변경된 파일 목록: ${changedFiles}"

                    // 각 서비스에 대해 변경 사항을 확인하고 해당하는 Job을 트리거
                    allServices.each { serviceName ->
                        if (hasServiceChanged(serviceName, changedFiles)) {
                            echo "${serviceName}에서 변경 사항 감지됨. 빌드를 시작합니다..."
                            // 각 서비스 이름에 해당하는 Jenkins Job을 호출
                            // 예: 'account' Job, 'payment' Job 등
                            build job: serviceName + "-service", wait: true // wait: true로 변경하여 작업을 순차적으로 실행
                        } else {
                            echo "${serviceName}에서 변경 사항 없음. 빌드를 건너<binary data, 1 bytes><binary data, 1 bytes><binary data, 1 bytes>니다."
                        }
                    }
                }
            }
        }
    }
}

/**
 * 변경된 파일 목록을 반환하는 함수
 * @return 변경된 파일 경로의 리스트
 */
def getChangedFiles() {
    // 가장 최근 커밋에서 변경된 파일 목록을 가져옴
    return sh(returnStdout: true, script: 'git diff-tree --no-commit-id --name-only -r HEAD').trim().tokenize('\n')
}

/**
 * 특정 서비스 디렉토리에 변경이 있었는지 확인하는 함수
 * @param serviceDirectory 확인할 서비스의 디렉토리 이름 (예: 'account')
 * @param changedFiles 변경된 파일 경로의 리스트
 * @return 변경 사항이 있으면 true, 없으면 false
 */
def hasServiceChanged(String serviceDirectory, List changedFiles) {
    // 변경된 파일 목록을 순회
    for (String file in changedFiles) {
        // 'common' 디렉토리 파일이 변경되면 모든 서비스에 대해 빌드를 트리거하기 위해 true를 반환
        if (file.startsWith("common/")) {
            return true
        }
        // 파일 경로가 해당 서비스 디렉토리로 시작하는지 확인
        if (file.startsWith("${serviceDirectory}/")) {
            return true // 하나라도 일치하면 즉시 true 반환
        }
    }
    return false // 변경 사항이 없으면 false 반환
}

