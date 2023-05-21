extends FileDialog

func _on_plys_instance_playback_end() -> void:
	show()

func _on_canceled() -> void:
	get_tree().quit()
