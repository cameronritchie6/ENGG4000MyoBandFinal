using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class Boss1 : MonoBehaviour
{
    public GameObject BlueTorpedo;
	public GameObject BeamWeapon;
	public GameObject HyperBeam;
	private Vector2 PFPos;
	private Vector2 down = Vector2.down;
	private Vector2 up = Vector2.up;
	private Vector2 left = Vector2.left;
	private float health = 100.0f;
	private float T = 0.0f;
	private float T2 = 0.0f;
	private float T3 = 0.0f;
	private float T4 = 0.0f;
	private float WaitT = 3.0f;
	private float WaitT2 = 4.5f;
	private float WaitT3 = 6.0f;
	
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
		T4 = T4 + Time.deltaTime;
		PFPos = transform.position;
		
		if(health <= 0.0f){
			
			text.SendMessage("bonusScore");
			text.SendMessage("bonusScore");
			text.SendMessage("bonusScore");
			text.SendMessage("bonusScore");
			text.SendMessage("bonusScore");
			text.SendMessage("bonusScore");
			text.SendMessage("bonusScore");
			text.SendMessage("saveScore");
			SceneManager.LoadScene("LevelComplete");
			Destroy(gameObject);
		}
		
		
		movement1();
			
		
		
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
		if(T4 > WaitT3){
			fireHyperBeam();
		}
	}
	
	
	
    public void firePrimary(){
		
		Instantiate(BlueTorpedo, (PFPos + new Vector2(-3.0f,-0.2f)), Quaternion.identity);
		T = T - WaitT;
	}
	
	public void fireBeam(){
		
		Instantiate(BeamWeapon, (PFPos + new Vector2(-5.0f,-1.5f)), q);
		Instantiate(BeamWeapon, (PFPos + new Vector2(-5.0f,1.5f)), q);
		T3 = T3 - WaitT2;
	}
	
	public void fireHyperBeam(){
		Instantiate(HyperBeam, (PFPos + new Vector2(-15.0f,-0.05f)), q);
		T4 = T4 - WaitT3;
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
	
	
}
