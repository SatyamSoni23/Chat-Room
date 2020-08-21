<?php
namespace MyApp;
use Ratchet\MessageComponentInterface;
use Ratchet\ConnectionInterface;

class Chat implements MessageComponentInterface {
	
	private $clients;
    //Inside constructer class we will instantiate client object
	public function __construct(){
		$localIP = getHostByName(getHostName());
		echo "Server Starting....\n".$localIP."\n";
		$this->clients = array();
	}	
    //We will store all the new connection in these client's array
    
    //Call when their is any connection in the socket
	public function onOpen(ConnectionInterface $conn) {
        $this->clients[] = $conn;
		echo "New Connection";
    }
	//Call when connection sent any message to the server
    public function onMessage(ConnectionInterface $from, $msg) {
        //Whenever their is new message we will echo that message
		//to every single client that has been connected to the server
		foreach($this->clients as $client){
			if($client!=$from){
				//this will prevent the echoing the message to the sender
				$client->send($msg);
			}	
		}	
		
    }
	//Call when the connecton closes
    public function onClose(ConnectionInterface $conn) {
		echo "Connection Closed";
    }
	//Call when their is any error occurs
    public function onError(ConnectionInterface $conn, \Exception $e) {
		echo $e->getMessage();
    }
}