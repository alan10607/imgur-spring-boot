# imgur-spring-boot
Demo of Imgur authorization, upload and refresh token for Spring Boot


## Imgur OAuth flows

```
           +----------+   1. Get token authorization       +---------------------+
           |          |  --------------------------------> |                     |
           |          |   2. Get access token              | Imgur Authorization |
           |          |  <-------------------------------- |                     |
           |          |                                    +---------------------+
3. User    |          |
 request   |          |   4. Upload img with access token  +---------------------+
---------> |          |  --------------------------------> |                     |
           |  server  |   5. Get img url                   | Imgur Resource      |
           |          |  <-------------------------------- |                     |
           |          |                                    +---------------------+
           |          |
           |          |   6. Re-auth with refresh token    +---------------------+
           |          |  --------------------------------> |                     |
           |          |   7. Get access token              | Imgur Authorization |
           |          |  <-------------------------------- |                     |
           +----------+                                    +---------------------+
```