using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using UnityEngine;

public class PLYSequenceReader : MonoBehaviour {
    public string plyFile;
    private Mesh _mesh;

    private StreamReader _reader;

    private void Start() {
        _mesh = new Mesh();
        _mesh.MarkDynamic();
        GetComponent<MeshFilter>().mesh = _mesh;
        // Check if the ply File is null before attempting to open it.
        if (plyFile != null) {
            _reader = new StreamReader(plyFile);
            string line = _reader.ReadLine();
            if (!line.Equals("plys")) {
                throw new FormatException("File did not have plys header");
            }

            List<string> sequenceHeader = new List<string>();

            while (!line.Equals("end_sequence_header")) {
                line = _reader.ReadLine();
                sequenceHeader.Add(line);
            }

            InvokeRepeating(nameof(RenderFrame), 0f,
                1 / float.Parse(sequenceHeader.Find(s => s.StartsWith("framerate "))
                    .Split(' ')[
                        1])); // This invoke repeating method runs once per frame time amount of time.
        }
        else {
            Debug.LogError("PLY file is not assigned");
        }
    }

    private void OnDestroy() {
        _reader.Close();
    }

    /*
    This method gets vertex and color information from the ".plys" file and 
    assigns the data to the verticies and colors List objects.
    */
    private void RenderFrame() {
        string line = _reader.ReadLine();
        if (!line.Equals("ply")) {
            throw new FormatException("Header not found.");
        }

        List<string> header = new List<string>();
        while (!line.Equals("end_header")) {
            line = _reader.ReadLine();
            header.Add(line);
        }

        int vertexCount = int.Parse(header.Find(s => s.StartsWith("element vertex ")).Split(" ")[2]);

        List<Vector3> vertices = new List<Vector3>(vertexCount);
        List<Color> colors = new List<Color>(vertexCount);

        switch (header.Find(s => s.StartsWith("format "))) {
            case "format ascii 1.0":
                List<string> lines = new List<string>(vertexCount);
                for (int i = 0; i < vertexCount; i++) {
                    lines.Add(_reader.ReadLine());
                }

                foreach (string vertex in lines.AsParallel()) {
                    string[] lineContents = vertex.Split(" ");
                    // Get the xyz components of the vertex and add it to the List of vertices.
                    float x = float.Parse(lineContents[0]);
                    float y = float.Parse(lineContents[1]);
                    float z = float.Parse(lineContents[2]);
                    // Now we grab the color from the file.
                    float r = float.Parse(lineContents[3]);
                    float g = float.Parse(lineContents[4]);
                    float b = float.Parse(lineContents[5]);
                    // Divide each color component by 255 to allow the Color constructor to parse correctly.
                    vertices.Add(new Vector3(x, y,
                        z)); //Add the new Vector3 that describes the point in space to the vector Array.
                    colors.Add(new Color(r / 255f, g / 255f, b / 255f));
                }

                _reader.ReadLine();
                break;
            case "format binary_little_endian 1.0":
                throw new NotImplementedException();
            case "format binary_big_endian 1.0":
                throw new NotImplementedException();
            default:
                throw new FormatException("Could not find file format.");
        }

        int[] indices = new int[vertexCount];
        Parallel.For(0, vertexCount, i => indices[i] = i);

        _mesh.Clear();
        _mesh.vertices = vertices.ToArray();
        _mesh.colors = colors.ToArray();
        _mesh.SetIndices(indices, MeshTopology.Points, 0);
    }
}