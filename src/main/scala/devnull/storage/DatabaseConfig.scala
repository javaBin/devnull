package devnull.storage

case class DatabaseConfig(driver: String = "org.postgresql.Driver",
                          connectionUrl: String = "jdbc:postgresql:devnull",
                          username: String = "devnull",
                          password: String = "devnull")
