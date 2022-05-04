using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class BirdOfPrey : MonoBehaviour
{
	
	public GameObject BlueTorpedo;
	private Vector2 BOPos;
	private Vector2 down = Vector2.down;
	private Vector2 up = Vector2.up;
	private Vector2 left = Vector2.left;
	private float health = 5.0f;
	private float T = 0.0f;
	private float T2 = 0.0f;
	private float WaitT = 3.0f;
	private Rigidbody2D Rigid;
	private GameObject text;
	private GameObject[] findText;
	
	void Start(){
		Rigid = GetComponent<Rigidbody2D>();
		findText = GameObject.FindGameObjectsWithTag("Score");
		text = findText[0];
	}
	void Update(){
		T = T + Time.deltaTime;
		T2 = T2 + Time.deltaTime;
		BOPos = transform.position;
		
		if(health <= 0.0f){
			
			text.SendMessage("bonusScore");
			text.SendMessage("bonusScore");
			
			Destroy(gameObject);
		}
		
		if(gameObject.tag == "BOP1"){
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
	}
	
    public void firePrimary(){
		
		Instantiate(BlueTorpedo, (BOPos + new Vector2(-0.7f,-0.2f)), Quaternion.identity);
		T = T - WaitT;
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
		if(col.gameObject.name == "PlayerShipLite" || col.gameObject.name == "PlayerShip"){
			Destroy(gameObject);
		}
		
	}
}
