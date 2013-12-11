"When Bitcoin reaches 1000$" Party Tickets
==========================================

This is the code used to sell Tickets for the http://btc1k.com party.

It is a demonstration of the power of the Bits of Proof's (BOP) Bitcoin technology. We deploy the white label Bitcoin merchant solution BopShop and advanced use of BOP Community Server and its client library.

The payment requests are generated and handled by BopShop. The callback points to the BopShopResource in this code. Once a payment is received, it will be split into two parts: 
  *   1 mB goes to a paper Bitcoin the guest prints and will act as a Ticket to the Event
  * 999 mB (less mining fee) goes to the Vault, that is a P2SH Address (starting with 3 instead of 1)

The P2SH Address can be controlled by 2 out of 3 keys. The GUI application btc1k-fx (also included in this project) allows signature of multi-party transactions while the server process only facilitaets the exchange of half-signed transactions, it does not have the control of the funds. Since the GUI reparses the transaction template, the server can not trick the GUI into paying for an other address.

Above procedure ensures that 
  * party guest may audit the safe storage and use of the funds on their own, 
  * funds can only be spent by a majority agreement between the organizer
  * reduce the risk of accidental loss by splitting the secret between several parties while one may be lost without consequence
  * reduce the risk of theft by not storing all information needed to access the funds on any single computer
  * tickets to the event demonstrate that Bitcoin is viable to hold and transfer rights, not just payments

Note that BOP only supports the technology, it has no access to the funds after they were collected and moved into the vault, it is also not responsible for the event.

BOP will extend this suite of applications with a mobile application to check ticket vailidity at entry.

Announcment of the ticket technology: https://bitcointalk.org/index.php?topic=285771.msg3797420#msg3797420
