Запуск проекта на debian:

1. Настроить DNS records -- создать A-запись, выбрав домен и указав ip-адрес сервера
2. Скопировать на сервер и запустить sh install_debian_12.sh
3. Сконфигурировать elk, исправив как минимум logstash.conf:
   ```
      input {
          beats {
              port => 5044
          }
      
          tcp {
              port => 50000
              codec => json_lines
          }
      }
      
      ## Add your filters / logstash plugins configuration here
      
      output {
          elasticsearch {
              hosts => "elasticsearch:9200"
              user => "logstash_internal"
              password => "${LOGSTASH_INTERNAL_PASSWORD}"
          }
      }
   ```
4. Установить и запустить [elk-стек](https://github.com/deviantony/docker-elk?tab=readme-ov-file#bringing-up-the-stack):

   ```sh
   git clone https://github.com/deviantony/docker-elk.git
   ```

   ```sh
   docker compose up setup
   ```

   ```sh
   docker compose up -d --always-recreate-deps --build --force-recreate
   ```

5. Настроить переменные окружения в файле .env
6. Поменять server_name на нужный в nginx.conf
7. Скомпилировать проект с помощью gradle-таска shadowJar, переименовать получившийся jar в app.jar
8. Скопировать на сервер app.jar, .env, docker-compose.yaml, Dockerfile, nginx.conf, start-app.sh
9. Запустить проект:

   ```sh
   sh start-app.sh
   ```