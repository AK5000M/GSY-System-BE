# GhostSpy API V1.0

## Description

    GhostSpy API is a RESTful API built using Node.js, Express.js, and MongoDB (via Mongoose). It provides endpoints for managing data related to a web application.

## Installations

    Before running the API, make sure you have Node.js and yarn added on your system. You can download them from Node.js website.

        ```
        yarn add  express
        yarn add -D nodemon ts-node typescript @types/express @types/node
        yarn add mongoose
        ```

## Getting Started

    1. Run the API in development mode using nodemon and TypeScript:
        ```
        yarn dev
        ```
        This command starts the API server using nodemon, which automatically restarts the server when changes are detected. TypeScript (ts-node) is used to transpile TypeScript code on the fly.


    2. Build the TypeScript code:
        ```
        yarn build
        ```
        This command compiles the TypeScript code into JavaScript, creating a dist directory with the compiled code.

    3. Start the API server:
        ```
        yarn start
        ```
        This command starts the API server using the compiled JavaScript code from the dist directory.

## Port

    The API server runs on port http://127.0.0.1:5000.

## Contributing

    Contributions are welcome! Feel free to submit issues or pull requests to improve the GhostSpy API.
