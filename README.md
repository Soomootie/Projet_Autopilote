# Projet_Autopilote
Projet Technologique Licence 3

# Authors 
Ahmad Boissetri Binzagr
Boutet Clement

##########
  TODO:  #
##########

# Fonctions:

/* envoie un message d'un certain type (eventuellement) */
push( (type) msg);

/* recupere n'importe quel message venant de n'importe quel capteur */ 
pull();

/* recupere un messge venant d'un capteur (id) passé en parametre */
pull_id(int id);

/* suppression des messages deja lus */
history();

/* enregistre l'ID du driver (lors de la connexion au serveur. */
register(   type_driver);

Envoi d'un message de la part d'un capteur:
Nom
ID unique 
Le serveur renvoit au capteur un ID.
 
