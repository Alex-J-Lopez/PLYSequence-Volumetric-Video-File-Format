class_name PLYSPlayback
extends MultiMeshInstance3D
var file: FileAccess

var timer: Timer = Timer.new()
var mesh: Mesh = SphereMesh.new()

signal playbackStart()
signal playbackEnd()

func play(path: String) -> int:
	file = FileAccess.open(path, FileAccess.READ)
	if file == null :
		push_error("PLYSPlayback: File could not be opened")
		return ERR_FILE_CANT_OPEN
	if file.get_line() != "plys":
		push_error("PLYSPlayback: File is not a PLYS file")
		return ERR_FILE_UNRECOGNIZED
	var sequenceHeaders: Dictionary = getHeader()
	var framedelay: float = 1 / sequenceHeaders["framerate"][0].to_float()
	timer.start(framedelay)
	playbackStart.emit()
	return OK
	
	
	
func renderFrame() -> void: 
	if file.get_position() >= file.get_length():
		print("End of file reached")
		cleanup()
	multimesh = MultiMesh.new()
	multimesh.mesh = mesh
	multimesh.use_colors = true
	multimesh.transform_format = MultiMesh.TRANSFORM_3D
	var line: String = file.get_line()
	if line != "ply":
		push_error("PLYSPlayback: File is not a PLY file, got: " + line)
		cleanup()
		return
	var headers: Dictionary = getHeader()
	var vertexCount: int
	for header in headers["element"]:
		if header.begins_with("vertex "):
			vertexCount = header.split(" ")[1].to_int()
			break
	if vertexCount == null:
		push_error("PLYSPlayback: No vertex count found")
		return
	multimesh.instance_count = vertexCount
	for i in range(vertexCount):
		line = file.get_line()
		#TODO add compatibility for different element parameter ordering and types
		var split: PackedStringArray = line.split(" ")
		multimesh.set_instance_transform(i, Transform3D().translated(Vector3(split[0].to_float(), split[1].to_float(), split[2].to_float())))
		multimesh.set_instance_color(i, Color8(split[3].to_int(), split[4].to_int(), split[5].to_int()))
	file.get_line()

func cleanup() -> void:
	timer.stop()
	file.close()
	print("Playback stopped")
	playbackEnd.emit()

func getHeader() -> Dictionary:
	var line: String = file.get_line()
	var headers: Dictionary = {}
	while line != "end_sequence_header" && line != "end_header":
		var split = line.split(" ", false, 1)
		if !headers.has(split[0]):
			headers[split[0]] = PackedStringArray()
		headers[split[0]].push_back(split[1])
		line = file.get_line()
	return headers

func _ready() -> void:
	add_child(timer)
	timer.timeout.connect(renderFrame)
	var material: BaseMaterial3D = ORMMaterial3D.new()
	material.vertex_color_use_as_albedo = true
	material.albedo_color = Color.WHITE
	mesh.material = material
	mesh.radius = 0.007
	mesh.height = 0.007
	mesh.radial_segments = 1
	mesh.rings = 1


func _on_file_dialog_file_selected(path: String) -> void:
	play(path)
