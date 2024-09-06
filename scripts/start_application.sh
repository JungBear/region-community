#!/bin/bash
export MYSQL_ID=$(aws ssm get-parameter --name /region-community/MYSQL_ID --with-decryption --query Parameter.Value --output text --region ap-northeast-2)
export MYSQL_PWD=$(aws ssm get-parameter --name /region-community/MYSQL_PWD --with-decryption --query Parameter.Value --output text --region ap-northeast-2)
cd /home/ec2-user/app/backend
nohup ./gradlew bootRun > /dev/null 2>&1 &

cd /home/ec2-user/app/frontend
nohup npx serve -s build -l 3000 > /dev/null 2>&1 &