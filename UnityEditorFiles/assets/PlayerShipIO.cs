using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;
using UnityEngine.UI;

public class PlayerShipIO : MonoBehaviour
{
	private Rigidbody2D Rigid;
	private Vector2 Vector2 = Vector2.zero;
	private Vector2 Vector2Down = Vector2.zero;
	public GameObject OrangePlasmaShot;
	public GameObject OrangeTorpedo;
	public Image image;
	private GameObject text;
	private GameObject[] findText;
	
	private float T = 0.0f;
	private float WaitT = 0.2f;
	private float CoolDown = 0.0f;
	private float CoolWait = 2.0f;
	private float CorFact = 25.0f;
	private Vector2 PP = Vector2.zero;
	private float maxVelocity = 2;
	public float Health = 10.0f;
	private bool Boss = false;
	private bool upButtonIsDown = false;
	private bool downButtonIsDown = false;
	
    // Start is called before the first frame update
    void Start()
    {
        Rigid = GetComponent<Rigidbody2D>();
		Vector2 = Vector2.left;
		Vector2Down = Vector2.down;
		findText = GameObject.FindGameObjectsWithTag("Score");
		text = findText[0];
		
    }

    // Update is called once per frame
   
	public void LateUpdate(){
		if(Boss == false){
			Rigid.MovePosition(Rigid.position + (Vector2 * (-4)) * Time.fixedDeltaTime);
		}
		
		if(Health <= 0.0f){
			
			text.SendMessage("saveScore");
			SceneManager.LoadScene("GameOverSG");
		}
		PP = transform.position;
		T += Time.deltaTime;
		CoolDown += Time.deltaTime;
		
		if(T > WaitT){
			firePrimary();
		}
		
		if(Input.GetKeyDown(KeyCode.Space)){
			if(CoolDown > CoolWait){
				fireSecondary();
			}
		}
		if(Input.GetKey(KeyCode.W)){
			pressUpButton();
		}
		else if(Input.GetKey(KeyCode.S)){
			pressDownButton();
		}
		if(!Input.GetKey(KeyCode.W)){
			releaseUpButton();
		}
		if(!Input.GetKey(KeyCode.S)){
			releaseDownButton();
		}
		
		if(upButtonIsDown == true){
			ThrustUp(1f);
		}
		else if(downButtonIsDown == true){
			ThrustUp(-1f);
		}
		
		
		
	}
	
	public void pressUpButton(){
		upButtonIsDown = true;
	}
	public void pressDownButton(){
		downButtonIsDown = true;
	}
	public void releaseUpButton(){
		upButtonIsDown = false;
	}
	public void releaseDownButton(){
		downButtonIsDown = false;
	}
	
	public void firePrimary(){
		
		Instantiate(OrangePlasmaShot, (PP + new Vector2(0.7f,0.2f)), Quaternion.identity);
		T = T - WaitT;
	}
	public void fireSecondary(){
		
		Instantiate(OrangeTorpedo, (PP + new Vector2(0.7f,0)), Quaternion.identity);
		CoolDown = 0.0f;
	}
	private void ClampVelocity(){
		float y = Mathf.Clamp(Rigid.velocity.y,-maxVelocity,maxVelocity);
		Rigid.velocity = new Vector2(0,y);
	}
	private void ThrustUp(float amount){
		Vector2 force = Vector2.up * amount * CorFact;
		Rigid.AddForce(force);
	}
	private void OnCollisionEnter2D(Collision2D collision) { 
        if(collision.gameObject.tag == "Finish"){
			Boss = true;
			CorFact = 1.0f;
		}
		else if (!(collision.gameObject.tag == "Boundary" || collision.gameObject.tag == "PowerUp")){
			Health--;
			image.BroadcastMessage("changeHealth", Health);
			findText = GameObject.FindGameObjectsWithTag("Score");
			text = findText[0];
			text.SendMessage("penalty");
			
		}
    }
	private void OnTriggerEnter2D(Collider2D collision){
		if(collision.gameObject.tag == "BP"){	
			Health--;
			image.BroadcastMessage("changeHealth", Health);
			findText = GameObject.FindGameObjectsWithTag("Score");
			text = findText[0];
			text.SendMessage("penalty");
		}
		else if(collision.gameObject.tag == "BT"){
			Health = Health - 2.0f;
			image.BroadcastMessage("changeHealth", Health);
			findText = GameObject.FindGameObjectsWithTag("Score");
			text = findText[0];
			text.SendMessage("penalty");
		}
		else if(collision.gameObject.tag == "IB"){
			Health = Health - 3.0f;
			image.BroadcastMessage("changeHealth", Health);
			findText = GameObject.FindGameObjectsWithTag("Score");
			text = findText[0];
			text.SendMessage("penalty");
		}
		else if(collision.gameObject.tag == "HB"){
			Health = Health - 5.0f;
			image.BroadcastMessage("changeHealth", Health);
			findText = GameObject.FindGameObjectsWithTag("Score");
			text = findText[0];
			text.SendMessage("penalty");
		}
		else if(collision.gameObject.tag == "Lollipop"){
			
		}
		else{
			Health--;
			image.BroadcastMessage("changeHealth", Health);
			findText = GameObject.FindGameObjectsWithTag("Score");
			text = findText[0];
			text.SendMessage("penalty");
		}
	}
}
