#!/bin/bash
cd /home/ec2-user/app/backend
nohup java -jar *.jar > /dev/null 2>&1 &

cd /home/ec2-user/app/frontend
nohup npx serve -s build -l 3000 > /dev/null 2>&1 &