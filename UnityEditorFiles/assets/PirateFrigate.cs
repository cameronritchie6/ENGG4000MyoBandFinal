using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class PirateFrigate : MonoBehaviour
{
    public GameObject BlueTorpedo;
	public GameObject BeamWeapon;
	private Vector2 PFPos;
	private Vector2 down = Vector2.down;
	private Vector2 up = Vector2.up;
	private Vector2 left = Vector2.left;
	private float health = 10.0f;
	private float T = 0.0f;
	private float T2 = 0.0f;
	private float T3 = 0.0f;
	private float WaitT = 3.0f;
	private float WaitT2 = 4.5f;
	private Rigidbody2D Rigid;
	private GameObject text;
	private GameObject[] findText;
	private Quaternion q = Quaternion.Euler(0,0,90);
	
	void Start(){
		Rigid = GetComponent<Rigidbody2D>();
		findText = GameObject.FindGameObjectsWithTag("Score");
		text = findText[0];
	}
	void Update(){
		T = T + Time.deltaTime;
		T2 = T2 + Time.deltaTime;
		T3 = T3 + Time.deltaTime;
		PFPos = transform.position;
		
		if(health <= 0.0f){
			
			text.SendMessage("bonusScore");
			text.SendMessage("bonusScore");
			text.SendMessage("bonusScore");
			Destroy(gameObject);
		}
		
		if(gameObject.tag == "PF1"){
			movement1();
		}
		else{
			movement2();
		}
	}
	
	public void movement1(){
		if(T > WaitT){
			firePrimary();
		}
		
		if(T2 < WaitT){
			Rigid.MovePosition(Rigid.position + (down * (4)) * Time.fixedDeltaTime);
		}
		else if(T2 < 2*WaitT){
			Rigid.MovePosition(Rigid.position + (up * (4)) * Time.fixedDeltaTime);
		}
		else{
			T2 = T2 - 2*WaitT;
		}
		
		if(T3 > WaitT2){
			fireBeam();
		}
	}
	
	public void movement2(){
		if(T > WaitT){
			firePrimary();
		}
		
		if(T2 < WaitT){
			Rigid.MovePosition(Rigid.position + (left * (4)) * Time.fixedDeltaTime);
		}
		else if(T2 < 2*WaitT){
			Rigid.MovePosition(Rigid.position + (left * (-4)) * Time.fixedDeltaTime);
		}
		else{
			T2 = T2 - 2*WaitT;
		}
		if(T3 > WaitT2){
			fireBeam();
		}
	}
	
    public void firePrimary(){
		
		Instantiate(BlueTorpedo, (PFPos + new Vector2(-1.5f,-0.2f)), Quaternion.identity);
		T = T - WaitT;
	}
	
	public void fireBeam(){
		
		Instantiate(BeamWeapon, (PFPos + new Vector2(-1.7f,-1.0f)), q);
		Instantiate(BeamWeapon, (PFPos + new Vector2(-1.7f,1.0f)), q);
		T3 = T3 - WaitT;
	}
	
	private void OnTriggerEnter2D(Collider2D col){
		if(col.gameObject.name == "OPS"){
			health = health-1.0f;
		}
		else if(col.gameObject.tag == "OPT"){
			health = health - 5.0f;
		}
		else{
			health = health - 0.5f;
		}
		
	}
	
	private void OnCollisionEnter2D(Collision2D col){
		if(col.gameObject.name == "PlayerShipLite"){
			Destroy(gameObject);
		}
		
	}
}
