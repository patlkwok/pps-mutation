var patterns = [];
var actions = [];
var colorScheme = {
	'a' : "red",
	'c' : "blue",
	'g' : "green",
	't' : "#D100FF"
};

function process(data) {
	// console.log(data);
	var terms = data.split(",");
	var name = terms[0];
	var refresh = Number(terms[1]);
	var nexp = Number(terms[2]);
	var nguess = Number(terms[3]);
	var score = terms[4];
	var genome = terms[5];
	var mutated = terms[6];
	var tpats = terms[7];
	var tacts = terms[8];
	var gpats = terms[9];
	var gacts = terms[10];

	document.getElementById("player").innerHTML = "Player: " + name;
	document.getElementById("nexp").innerHTML = "Number of experiments: " + nexp;
	document.getElementById("nguess").innerHTML = "Number of guesses: " + nguess;
	document.getElementById("score").innerHTML = score;

	if (score.length > 0) {
		if (score == "Score pending") refresh = 5000;
		else refresh = -1;
		// refresh = -1;
	}

	var i, j;

// Show tPatterns
	if (tpats.length > 0) {
		patterns = tpats.split("@");
		actions = tacts.split("@");
		var table = document.getElementById("target");
		table.innerHTML = "";
		for (i = 0; i < patterns.length; ++ i) {
			var row = table.insertRow(-1);
			var c1 = row.insertCell(0);
			var c2 = row.insertCell(1);
			c1.innerHTML = patterns[i];
			c2.innerHTML = actions[i];
		}
	}
	if (gpats.length > 0) {
		var tp = gpats.split("@");
		var ta = gacts.split("@");
		var table = document.getElementById("guess");
		table.innerHTML = "";
		for (i = 0; i < tp.length; ++ i) {
			var highlighted = false;
			for (j = 0; j < patterns.length; ++ j)
				if (tp[i] == patterns[j] && ta[i] == actions[j]) {
					highlighted = true;
					break;
				}
			var row = table.insertRow(-1);
			var c1 = row.insertCell(0);
			var c2 = row.insertCell(1);
			if (highlighted) {
				c1.classList.add("highlighted");
				c2.classList.add("highlighted");
			}
			c1.innerHTML = tp[i];
			c2.innerHTML = ta[i];
		}
	}

	if (genome.length > 0) {
		var result = "";
		var width = document.getElementById("lcol").offsetWidth;

		var npl = Math.floor(width / 13);
		var nl = Math.ceil(genome.length / npl);

		for (i = 0; i < nl; ++ i) {
			var p1 = "", p2="";
			for (j = i * npl; j < (i + 1) * npl && j < genome.length; ++ j) {
				if (genome[j] == mutated[j]) {
					p2 += "&nbsp";
					p1 += genome[j].fontcolor(colorScheme[genome[j]]);
				} else {
					p1 += '<font class="hfont" color="' + colorScheme[genome[j]] + '">' + genome[j] + '</font>';
					p2 += '<font class="hfont" color="' + colorScheme[mutated[j]] + '">' + mutated[j] + '</font>';
				}
			}
			result += '<p>' + p2 + '</p>' + '<p class="p1">' + p1 + '</p>';
		}
		document.getElementById("lcol").innerHTML = result;
	}

	return refresh;
}

var latest_version = -1;

function ajax(version, retries, timeout) {
	console.log("Version " + version);
	var xhttp = new XMLHttpRequest();
	xhttp.onload = (function() {
			var refresh = -1;
			try {
				if (xhttp.readyState != 4)
					throw "Incomplete HTTP request: " + xhttp.readyState;
				if (xhttp.status != 200)
					throw "Invalid HTTP status: " + xhttp.status;
				//console.log(xhttp.responseText);
				refresh = process(xhttp.responseText);
				//console.log(refresh);
				if (latest_version < version)
					latest_version = version;
				else refresh = -1;
			} catch (message) {
				alert(message);
			}
			if (refresh >= 0)
				setTimeout(function() { ajax(version + 1, 10, 100); }, refresh);
		});
	xhttp.onabort = (function() { location.reload(true); });
	xhttp.onerror = (function() { location.reload(true); });
	xhttp.ontimeout = (function() {
			if (version <= latest_version)
				console.log("AJAX timeout (version " + version + " <= " + latest_version + ")");
			else if (retries == 0)
				location.reload(true);
			else {
				console.log("AJAX timeout (version " + version + ", retries: " + retries + ")");
				ajax(version, retries - 1, timeout * 2);
			}
		});
	xhttp.open("GET", "data.txt", true);
	xhttp.responseType = "text";
	xhttp.timeout = timeout;
	xhttp.send();
}

ajax(1, 10, 100)
