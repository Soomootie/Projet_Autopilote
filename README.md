# Projet_Autopilote
Projet Technologique Licence 3

# Authors 
Ahmad Boissetri Binzagr

Boutet Clement

#### TODO ######

# Fonctions:

/* envoie un message d'un certain type (eventuellement) */

send( (type) msg);

/* recupere n'importe quel message venant de n'importe quel capteur */ 

recv();

/* recupere un messge venant d'un capteur (id) passé en parametre */

recv_id(int id);

/* attribution d'un identifiant selon le type du driver */

id_driver(type_driver);

/* attribution d'un identifiant (appellee dans id_driver) */

single_key(type_driver id);

/* suppression du dernier message deja lu */

delete_msg();

/* suppression des messages envoyés par un certain capteur */

delete_msg_id(ref id);

/* suppression des messages envoyés par un certain type de capteur */

delete_msg_type(type-driver id);

/* liste tous les messages envoyés dans le bus */

history();

/* enregistre l'ID du driver (lors de la connexion au serveur. */
register(   type_driver);

Envoi d'un message de la part d'un capteur:
Nom 
ID unique 
Le serveur renvoit au capteur un ID. 
 
