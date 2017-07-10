# Blockchain Car Demo

[![Build Status](https://travis-ci.org/EGabb/Car-Trading-Blockchain.svg?branch=master)](https://travis-ci.org/EGabb/Car-Trading-Blockchain)

## Env Setup

You may need to clean your docker env first (radical, hard-core way):
```
sudo systemctl stop docker
sudo rm -rf /var/lib/docker/
sudo systemctl start docker
```

Then type the following command from the project root:
```
cd fixtures
bash build_image.sh
```

This will assemble and install the docker image for the car API. To download the required fabric images and spin up the fabric network just run:
```
bash fabric.sh restart
```

## Install and Instantiate CC
To test the car API (install & instantiate the cc) execute:
```
bash fixtures/instantiate_car_cc.sh
```

## Create Cars with the REST API
You can also try to create a car now with the rest api:
```
curl http://127.0.0.1:8080/rest/createCar;
```

To check out your newly created car, log into one of the docker containers of `org1` (either `peer0` or `peer1`) and query the test car:
```
local$           docker exec -it peer0.org1.example.com bash
root@peer0.org1# peer chaincode invoke -n car_cc_go -C foo -c '{"Args":["readCar", "test_user1", "garage", "WVW ZZZ 6RZ HY26 0780"]}'
```

If you encounter problems, try a `docker rm $(docker ps -aq)` to remove all containers from time to time.

## CC Development
To test if cc builds locally with most recent fabric-preview.:
```
go get github.com/hyperledger/fabric
cd $GOPATH/src/github.com/hyperledger/fabric
git checkout -b v1.0.0-preview origin/v1.0.0-preview
```

To see if the cc actually builds, clone this repo and execute from that folder:
```
cd chaincode/src/github.com/car_cc/
go build
```

To run all tests:
```
go test
```

Or to run only some tests (TestTransferCar test in this case):
```
go test -run TestTransferCar
```

## Documentation
On [Google Drive](https://docs.google.com/document/d/1U7C9dJmDg_-l5gKeseZEKqc5ooru2wMxZ8BwhkbjIbk/edit?usp=sharing)

## References
Docker setup from [yeasy(v1.0)](https://github.com/yeasy/docker-compose-files/tree/master/hyperledger/1.0).
