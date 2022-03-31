FROM alpine
RUN apk update
RUN apk add py3-pip
RUN apk add yamllint && apk add nodejs
RUN apk add --update npm
RUN npm install jsonlint -g
RUN npm install @redocly/openapi-cli -gn