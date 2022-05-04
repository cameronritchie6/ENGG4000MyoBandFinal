using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.Playables;
using UnityEngine.InputSystem;






public class PlayerIO : MonoBehaviour
{
	[SerializeField] private LayerMask Ground; 
	[SerializeField] private Transform GroundCheck;
	private float GroundedRadius = 0.2f;
	private float JumpF = 600;
	private Rigidbody2D Rigid;
	private bool isGround;
	PlayerControls controls;

	private void Awake(){
		Rigid = GetComponent<Rigidbody2D>();
		controls = new PlayerControls();
		controls.Gameplay.Jump.performed += ctx => Jump();
		controls.Gameplay.Jump2.performed += ctx => Jump();
	}
    
	public void Jump(){
		
		Collider2D[] colliders = Physics2D.OverlapCircleAll(GroundCheck.position, GroundedRadius, Ground);
			for (int i = 0; i < colliders.Length; i++){
				if (colliders[i].gameObject != gameObject){
					isGround = true;
				
				}
			}
			if(isGround){
				isGround = false;
				Rigid.AddForce(new Vector2(0f, JumpF));
			}
	}
	
	void OnEnable(){
		controls.Gameplay.Enable();
	}
	
	void OnDisable(){
		controls.Gameplay.Disable();
	}
}
