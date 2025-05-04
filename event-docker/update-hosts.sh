    #!/bin/bash

    # 현재 스크립트의 파일 이름 가져오기
    SCRIPT_PATH="$0"

    # 권한 부여 확인 및 적용
    if [ ! -x "$SCRIPT_PATH" ]; then
      echo "==> The script does not have execute permission. Adding execute permission..."
      chmod +x "$SCRIPT_PATH"
      echo "==> Execute permission added to the current script ($SCRIPT_PATH). Please re-run the script."
      exit 0
    else
      echo "==> Execute permission already exists."
    fi

    # 실제 로직 시작
    echo "==> Updating /etc/hosts with Redis node entries..."

    # 추가할 호스트 엔트리 정의
    ENTRY="127.0.0.1 redis-node-1 redis-node-2 redis-node-3"

    # 중복 추가 방지: /etc/hosts 파일에 이미 엔트리가 있는지 확인
    if ! grep -q "redis-node-1" /etc/hosts; then
      echo "==> Adding Redis nodes to /etc/hosts..."

      # 주석 추가 및 엔트리 삽입
      echo -e "\n# Custom Redis nodes" | sudo tee -a /etc/hosts
      echo "$ENTRY" | sudo tee -a /etc/hosts > /dev/null

      echo "==> Redis nodes added successfully."
    else
      echo "==> /etc/hosts already contains Redis node entries. No changes made."
    fi