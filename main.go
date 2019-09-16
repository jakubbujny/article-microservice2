package main

import (
	"fmt"
	"net/http"
)

func main() {
	handler := http.NewServeMux()
	handler.HandleFunc("/api/hello", SayHello)
	err := http.ListenAndServe("0.0.0.0:8080", handler)
	if err != nil {
		panic(err)
	}
}

func SayHello(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, `Hello world from microservice2`)
}
