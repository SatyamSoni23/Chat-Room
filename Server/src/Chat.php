<?php
namespace MyApp;
use Ratchet\MessageComponentInterface;
use Ratchet\ConnectionInterface;

class Chat implements MessageComponentInterface {
	
	protected $clients;
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
		echo "New connection! ({$conn->resourceId})\n";
    }
	//Call when connection sent any message to the server
    public function onMessage(ConnectionInterface $from, $msg) {
		$numRecv = count($this->clients) - 1;
		 echo sprintf('Connection %d sending message "%s" to %d other connection%s' . "\n"
            , $from->resourceId, $msg, $numRecv, $numRecv == 1 ? '' : 's');
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
		$this->clients->detach($conn);
		echo "Connection {$conn->resourceId} has disconnected\n";
    }
	//Call when their is any error occurs
    public function onError(ConnectionInterface $conn, \Exception $e) {
		echo "An error has occurred: {$e->getMessage()}\n";
        $conn->close();
    }
}