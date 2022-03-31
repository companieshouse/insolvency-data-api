FROM alpine
RUN apk add py3-pip
RUN pip install --user yamllint
RUN apk update && apk add nodejs
RUN apk add --update npm
RUN npm install jsonlint -g
RUN npm install @redocly/openapi-cli -gn