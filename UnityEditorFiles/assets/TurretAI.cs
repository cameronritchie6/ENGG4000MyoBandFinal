using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class TurretAI : MonoBehaviour
{
    public GameObject Player;
	public GameObject Weapon;
	private Vector2 PP;
	private Vector2 TP;
	private Quaternion TR;
	private Quaternion R;
	private float yDisp;
	private float xDisp;
	private float rotation;
	private float i = 1;
	private float T = 0.0f;
	private float waitT = 0.05f;
	private float fireT = 0.0f;
	private float coolDownT = 1.0f;
	private bool BackSweep = false;
	private GameObject text;
	private GameObject[] findText;
	

    // Update is called once per frame
    void Update()
    {
		T = T + Time.deltaTime;
		fireT = fireT + Time.deltaTime;
        PP = Player.transform.position;
		TP = transform.position;
		TR = transform.rotation;
		
		
		if(T > waitT){
			if(i < 90f && BackSweep == false){
				i = i + 3f;
			}
			else if(i >= 90f){
				BackSweep = true;
				i = i - 3f;
			}
			else if(i < 90f && i > -90f && BackSweep == true){
				i = i - 3f;
			}
			else if(i <= -90f){
				BackSweep = false;
			}
			T = T - waitT;
		}
		
		TR = rotateTurret(i);
		transform.rotation = TR;
		
		if(fireT > coolDownT){
			Vector2 Aim = Vector2.zero;
			float angle = 90f;
			if(i==angle){
				Aim = new Vector2(-1,0);
			}
			else if(i==-angle){
				Aim = new Vector2(1,0);
			}
			else if(i==0f){
				Aim = new Vector2(0,1);
			}
			else if(i>0f){
				Aim = new Vector2(Mathf.Cos(angle-i), Mathf.Abs(Mathf.Sin(angle - i)));
			}
			else{
				Aim = new Vector2(Mathf.Cos(-angle-i), Mathf.Abs(Mathf.Sin(-angle - i)));
			}
			
			Aim = 0.5f * Aim;
			GameObject BP = Instantiate(Weapon, (TP + Aim), Quaternion.Euler(0,0,i)) as GameObject;
			BP.SendMessage("TheStart",Aim);
			fireT = fireT - coolDownT;
		}
    }
	
	
	public Quaternion rotateTurret(float i){
		
		TR = Quaternion.Euler(0,0,i);
		
		return TR;
	}
	
	public void OnTriggerEnter2D(Collider2D collision){
		findText = GameObject.FindGameObjectsWithTag("Score");
		text = findText[0];
		text.SendMessage("bonusScore");
		Destroy(gameObject);
		
	}
	
	public void OnCollisionEnter2D(Collision2D collision){
		findText = GameObject.FindGameObjectsWithTag("Score");
		text = findText[0];
		text.SendMessage("bonusScore");
		Destroy(gameObject);
		
	}
}
