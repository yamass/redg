(function() {
    var menuOpen = false;
    var navigationButtons = document.querySelectorAll(".navigation-menu,#mobile-navigation-block");
    for (var i = 0; i < navigationButtons.length; i++) {
        navigationButtons[i].addEventListener("click", function () {
            var nav = document.getElementById("mobile-navigation");
            var block = document.getElementById("mobile-navigation-block");
            if (menuOpen) {
                nav.style.left = "-70vw";
                block.style["transition-delay"] ="";
                block.style.visibility = "hidden";
                block.style.opacity = "0";
            } else {
                nav.style.left = "0";
                block.style["transition-delay"] ="0s";
                block.style.visibility = "visible";
                block.style.opacity = ".7";
            }
            menuOpen = !menuOpen;
        });
    }
})();