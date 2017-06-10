# money-transfer

Build status: [![CircleCI](https://circleci.com/gh/ilya-murzinov/money-transfer/tree/master.png?style=shield)](https://circleci.com/gh/ilya-murzinov/money-transfer)

Purely functional Scala implementation of REST API for money transfer between accounts using [`Finch`][finch], [`Circe`][circe], [`Doobie`][doobie] and [`Scalatest`][scalatest] with [`Featherbed`][featherbed] for testing.

To run simply execute ```sbt run```.

To package executable binary run ```sbt clean test stage```. Executable will be installed in ```./target/universal/stage/bin/money-transfer```

API root URL: [http://localhost:8080/api/](http://localhost:8080/api/)

The following methods implemented:

## ```PUT /account```

  Creates new account, returns account in response.

  Example request:
  ```
  {
    "firstName": "John",
    "lastName": "Smith",
    "amount": 100
  }
  ```
  Example response:
  ```
  Status: 200
  {
    "id": "ed8d39bf-765d-4d56-af5f-5ffa5f9b3264",
    "firstName": "John",
    "lastName": "Smith",
    "amount": 100.0
  }
  ```

## ```GET /account/$id```

  Example response:
  ```
  Status: 200
  {
    "id": "ed8d39bf-765d-4d56-af5f-5ffa5f9b3264",
    "firstName": "John",
    "lastName": "Smith",
    "amount": 100.0
  }
  ```
  Possible errors:
  * Account not found:
    ```
    Status: 404
    {
      "message": "Account with id 'ed8d39bf-765d-4d56-af5f-5ffa5f9b3264' not found"
    }
    ```

## ```POST /transaction```
  Transfers money from one account to another. Returns successful transaction.

  Example request:
  ```
  {
    "fromId": "ed8d39bf-765d-4d56-af5f-5ffa5f9b3264",
    "toId": "9dcc42e7-fde6-477a-bbda-3700c7879a44",
    "amount": 10
  }
  ```
  Example response:
  ```
  Status: 200
  {
    "fromId": "ed8d39bf-765d-4d56-af5f-5ffa5f9b3264",
    "toId": "9dcc42e7-fde6-477a-bbda-3700c7879a44",
    "amount": 10
  }
  ```

  Possible errors:
  * Account not found
    ```
    Status: 404
    {
      "message": "Account with id 'ed8d39bf-765d-4d56-af5f-5ffa5f9b3264' not found"
    }
    ```
  * Can't transfer negative amount of money:
    ```
    Status: 400
    {
      "message": "Can't transfer non-positive amount of money"
    }
    ```
  * Not enough  money to transfers:
    ```
    Status: 400
    {
      "message": "Account with id 'ed8d39bf-765d-4d56-af5f-5ffa5f9b3264' doesn't have enough money to transfer to account with id '9dcc42e7-fde6-477a-bbda-3700c7879a44'"
    }
    ```

[finch]: https://github.com/finagle/finch
[circe]: https://github.com/circe/circe
[doobie]: https://github.com/tpolecat/doobie
[scalatest]: https://github.com/scalatest/scalatest
[featherbed]: https://github.com/finagle/featherbed
