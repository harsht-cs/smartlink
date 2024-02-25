const sidebar = document.querySelector(".sidebar");
const content = document.querySelector(".content");

const toogleSidebar = () => {

	console.log("Function is running");
	if (sidebar.style.display === "block") {

		sidebar.style.display = "none";
		content.style.marginLeft = "0%";

	} else {

		sidebar.style.display = "block";
		content.style.marginLeft = "20%";

	}
};
