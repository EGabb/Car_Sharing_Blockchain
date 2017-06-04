package main

import "crypto/rsa"

type Car struct {
    Certificate    *Certificate          `json:"certificate"`  // vehicle certificate issued by the DOT
    CreatedTs      int64                 `json:"created_ts"`   // birth date
    Vin            string                `json:"vin"`          // vehicle identification number
}

type User struct {
    Name           string                `json:"name"`
    KeyringEntries []KeyringEntry        `json:"keyring_entries"`
}

type KeyringEntry struct {
    PublicKey       rsa.PublicKey        `json:"public_key"`
    PrivateKey      rsa.PrivateKey       `json:"private_key"`
    CarTs           int64                `json:"car_ts"`
}

/*
 * Fahrzeugausweis
 *
 * The car certificate information is attested by the DOT
 */
type Certificate struct {
    Username        string `json:"username"`     // car owners name
    Insurer         string `json:"insurer"`      // the name of an insurance company
    Numberplate     string `json:"numberplate"`  // number plate ('AG 104 739')
    Vin             string `json:"vin"`          // vehicle identification number ('WVW ZZZ 6RZ HY26 0780')
    Color           string `json:"color"`
    Type            string `json:"type"`         // type: 'passenger car', 'truck', ...
    Brand           string `json:"brand"`
}

/*
 * Pruefungsbericht
 * (Form. 13.20 A)
 */
type CarAudit struct {
    CarTs                 int64      `json:"car_ts"`
    NumberOfDoors         string     `json:"number_of_doors"`     // '4+1' for a passenger car
    NumberOfCylinders     int        `json:"number_of_cylinders"` // 3, 4, 6, 8 ?
    NumberOfAxis          int        `json:"number_of_axis"`      // typically 2
    MaxSpeed              int        `json:"max_speed"`           // maximum speed as tested
}