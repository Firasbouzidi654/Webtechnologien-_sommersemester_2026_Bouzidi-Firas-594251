spring.application.name=KinderCareConnect

server.port=${PORT:10000}
server.address=0.0.0.0
server.servlet.context-path=/

spring.datasource.url=${DATABASE_URL:jdbc:h2:mem:testdb}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.flyway.enabled=true
spring.flyway.url=${DATABASE_URL:jdbc:h2:mem:testdb}
spring.flyway.baseline-on-migrate=true

spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000

logging.level.root=INFO
logging.level.de.htw_berlin=INFO

server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.compression.enabled=true