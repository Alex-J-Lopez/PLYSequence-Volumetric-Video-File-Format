extends Node


func _on_plys_instance_playback_start() -> void:
	get_parent().enabled = true
	get_parent()._ready()


func _on_plys_instance_playback_end() -> void:
	get_parent().enabled = false
	Input.mouse_mode = Input.MOUSE_MODE_VISIBLE
