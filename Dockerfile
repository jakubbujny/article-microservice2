FROM golang

ADD . /workspace

WORKDIR /workspace

CMD go run main.go