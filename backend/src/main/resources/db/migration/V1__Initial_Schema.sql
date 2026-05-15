spring.application.name=KinderCareConnect

server.port=${PORT:10000}
server.address=0.0.0.0
server.servlet.context-path=/

spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

logging.level.root=INFO
logging.level.de.htw_berlin=INFO

server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.compression.enabled=true