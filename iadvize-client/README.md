# IAdvize Client

The iAdvize client used to consume iAdvize APIs (Authentication and Data -GraphQL-)

**Selection of iadvize credential provider:**

`iadvize_credentials_provider_type` : Provider type used to create a credential provider instance

**For iAdvize credentials AWS provider:**

| Environment variables                          | Description                                          |
|------------------------------------------------|------------------------------------------------------|
| `aws_secret_manager_secret_version`            | the secret version<br/>Default=`AWSCURRENT`          |
| `aws_secret_manager_assumed_role_arn`          | the ARN (Amazon Resource Name) of role               |
| `aws_secret_manager_assumed_role_session_name` | the session name of the role                         |
| `aws_iadvize_credentials_secret_id`            | the secret name (NOT ITS ARN) of iAdvize credentials |
| `iadvize_credentials_provider_type`            | value = `AWS`                                        |




