// Jenkinsfile-orchestrator
pipeline {
    agent any

    stages {
        stage('Detect Changes for Account Service') {
            steps {
                script {
                 checkout scm;

                    // 이전 커밋과 현재 커밋(HEAD) 사이에 변경된 파일 목록을 가져옴
                    def changedFiles = getChangedFiles()
                    echo "Changed files: ${changedFiles}"

                    // account-service의 파일이 변경되었는지 확인
                    if (hasServiceChanged('account-service', changedFiles)) {
                        echo "Changes detected in account-service. Triggering build..."
                        // 'account-service-pipeline' Job을 호출
                        build job: 'account-service'
                    } else {
                        echo "No changes detected in account-service. Skipping build."
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
    // git diff 명령어를 사용하여 변경된 파일 목록을 가져옴
    // 'HEAD~1'은 바로 이전 커밋을 의미합니다.
    return sh(returnStdout: true, script: 'git diff --name-only HEAD~1 HEAD').trim().tokenize('\n')
}

/**
 * 특정 서비스 디렉토리에 변경이 있었는지 확인하는 함수
 * @param serviceDirectory 확인할 서비스의 디렉토리 이름 (예: 'account-service')
 * @param changedFiles 변경된 파일 경로의 리스트
 * @return 변경 사항이 있으면 true, 없으면 false
 */
def hasServiceChanged(String serviceDirectory, List changedFiles) {
    // 변경된 파일 목록을 순회하면서
    for (String file in changedFiles) {
        // 파일 경로가 해당 서비스 디렉토리로 시작하는지 확인
        if (file.startsWith("${serviceDirectory}/")) {
            return true // 하나라도 있으면 즉시 true 반환
        }
    }
    return false // 변경 사항이 없으면 false 반환
}
