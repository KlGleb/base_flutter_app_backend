Запуск проекта на debian:

1. Настроить DNS records -- создать A-запись, выбрав домен и указав ip-адрес сервера
2. Скопировать на сервер и запустить sh install_debian_12.sh
3. Установить и запустить [elk-стек](https://github.com/deviantony/docker-elk?tab=readme-ov-file#bringing-up-the-stack):

   ```sh
   git clone https://github.com/deviantony/docker-elk.git
   ```
   
   ```sh
   docker compose up setup
   ```

   ```sh
   docker compose up -d --always-recreate-deps --build --force-recreate
   ```

4. Настроить переменные окружения в файле .env
5. Поменять server_name на нужный в nginx.conf
6. Скомпилировать проект с помощью gradle-таска shadowJar, переименовать получившийся jar в app.jar
7. Скопировать на сервер app.jar, .env, docker-compose.yaml, Dockerfile, nginx.conf, start-app.sh
8. Запустить проект:

   ```sh
   sh start-app.sh
   ```