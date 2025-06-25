from textual.app import App, ComposeResult
from textual.containers import Vertical, Horizontal
from textual.widgets import Button, Input, Static, Checkbox, DirectoryTree
from textual.screen import ModalScreen
import json
import os

class FileBrowserScreen(ModalScreen):
    """A modal screen for browsing and selecting files."""
    
    def __init__(self, initial_path="./", target_input=None):
        super().__init__()
        self.initial_path = os.path.abspath(initial_path)
        self.target_input = target_input
        self.selected_path = None
    
    def compose(self) -> ComposeResult:
        yield Vertical(
            Static("Select a file or directory", id="browser-title"),
            Button("..", id="parent-dir", variant="default"),
            DirectoryTree(self.initial_path, id="file-tree"),
            Horizontal(
                Button("Select", id="select-btn", variant="primary"),
                Button("Cancel", id="cancel-btn"),
                id="browser-buttons"
            ),
            id="browser-container"
        )
    
    def on_mount(self) -> None:
        """Configure the directory tree to show parent directories."""
        tree = self.query_one("#file-tree", DirectoryTree)
        tree.show_root = True
        tree.show_guides = True
    
    def on_directory_tree_file_selected(self, event: DirectoryTree.FileSelected) -> None:
        """Handle file selection - just store the path, don't dismiss."""
        self.selected_path = str(event.path)
    
    def on_directory_tree_directory_selected(self, event: DirectoryTree.DirectorySelected) -> None:
        """Handle directory selection - just store the path, don't dismiss."""
        self.selected_path = str(event.path)
    
    def on_button_pressed(self, event: Button.Pressed) -> None:
        if event.button.id == "select-btn":
            # Use selected path or currently highlighted path
            if self.selected_path and self.target_input:
                self.target_input.value = self.selected_path
            elif self.target_input:
                tree = self.query_one("#file-tree", DirectoryTree)
                if tree.cursor_node:
                    self.target_input.value = str(tree.cursor_node.data.path)
            self.dismiss()
        elif event.button.id == "cancel-btn":
            self.dismiss()
        elif event.button.id == "parent-dir":
            # Navigate to parent directory
            current_path = self.query_one("#file-tree", DirectoryTree).path
            parent_path = os.path.dirname(current_path)
            if parent_path != current_path:  # Avoid infinite loop at root
                tree = self.query_one("#file-tree", DirectoryTree)
                tree.path = parent_path
                tree.reload()

class GenConfigApp(App):
    CSS = """
    Screen {
        layout: vertical;
        align: center middle;
    }
    #form {
        height: auto;
        width: 60%;
    }
    Input {
        margin: 1 0;
    }
    .path-container {
        height: auto;
        margin: 1 0;
    }
    .path-input {
        width: 1fr;
        margin: 0 1 0 0;
    }
    .path-button {
        width: auto;
        margin: 0;
    }
    .subsection {
        border: solid $primary;
        margin: 1 0;
        padding: 1;
        height: auto;
    }
    .subsection-title {
        text-style: bold;
        color: $primary;
        margin: 0 0 1 0;
    }
    #browser-container {
        width: 80%;
        height: 80%;
        border: thick $primary;
        background: $surface;
    }
    #browser-title {
        text-align: center;
        text-style: bold;
        margin: 1;
    }
    #file-tree {
        height: 1fr;
        margin: 1;
    }
    #browser-buttons {
        height: auto;
        align: center middle;
        margin: 1;
    }
    """

    form = {
        "bot": {
            "namespace": {"data_type": "text", "data": None},
            "bot_id": {"data_type": "text", "data": None},
            "file_location": {"data_type": "path", "data": None}
        },
        "em_setting": {
            "provider": {"data_type": "text", "data": None},
            "api_key": {
                "type": {"data_type": "text", "data": None},
                "secret": {"data_type": "text", "data": None}
            },
            "api_base": {"data_type": "text", "data": None},
            "api_version": {"data_type": "text", "data": None},
            "deployment_name": {"data_type": "text", "data": None},
            "model": {"data_type": "text", "data": None}
        },
        "vector_store_setting": {
            "provider": {"data_type": "text", "data": None},
            "host": {"data_type": "text", "data": None},
            "port": {"data_type": "integer", "data": None},
            "username": {"data_type": "text", "data": None},
            "password": {
                "type": {"data_type": "text", "data": None},
                "secret": {"data_type": "text", "data": None}
            },
            "database": {"data_type": "text", "data": None}
        },
        "data_csv_file": {"data_type": "path", "data": None},
        "document_index_name": {"data_type": "text", "data": None},
        "chunk_size": {"data_type": "integer", "data": None},
        "embedding_bulk_size": {"data_type": "integer", "data": None},
        "ignore_source": {"data_type": "bool", "data": None},
        "append_doc_title_and_chunk": {"data_type": "bool", "data": None}
    }

    def compose(self) -> ComposeResult:
        yield Static("Multi-Page Dynamic Form", id="title")
        yield Vertical(id="form")
        yield Horizontal(
            Button("Previous", id="prev"),
            Button("Next", id="next"),
            Button("Write to JSON", id="save"),
            id="buttons"
        )
        yield Button("Load EM settings from RAG Settings", id="load-em-settings")

    def on_mount(self) -> None:
        self.current_page = 0
        self.len_pages = 1
        for dict in self.form.values():
            if not "data_type" in dict:
                self.len_pages += 1
        self.load_form_page()

    @staticmethod
    def labelize(text: str) -> str:
        """Convert text to a label-friendly format."""
        text = text.replace("_", " ").lower()
        text = text.capitalize()
        return text

    def get_path_from_directory_tree_in_widget(self, widget: Input):
        """Open a fullscreen file browser to select a path."""
        initial_path = widget.value if widget.value else "./"
        file_browser = FileBrowserScreen(initial_path, widget)
        self.push_screen(file_browser)

    def gen_widgets(self, container, dict, dict_id=""):
        for key, value in dict.items():
            data_type = value.get("data_type")
            if data_type is None and self.current_page == self.len_pages - 1:
                continue
            if data_type is None:
                subsection = Vertical(classes="subsection", id=f"{dict_id}__{key}")
                container.mount(subsection) 
                title = Static(self.labelize(key), classes="subsection-title")
                subsection.mount(title) 
                self.gen_widgets(subsection, value, dict_id=f"{dict_id}__{key}")
                continue
            elif data_type == "bool":
                widget = Checkbox(label=self.labelize(key), value=value.get("data") if value.get("data") is not None else False, id=f"{dict_id}__{key}")
                container.mount(widget)
            elif data_type == "path":
                # Create horizontal container for path input and browse button
                path_container = Horizontal(classes="path-container")
                container.mount(path_container)
                widget = Input(value=value.get("data"), type="text", placeholder=f"{self.labelize(key)} ({self.labelize(data_type)})", id=f"{dict_id}__{key}", classes="path-input")
                button = Button("Browse", id=f"browse__{dict_id}__{key}", classes="path-button")
                path_container.mount(widget)
                path_container.mount(button)
            else:
                widget = Input(value=value.get("data"), type=data_type, placeholder=f"{self.labelize(key)} ({self.labelize(data_type)})", id=f"{dict_id}__{key}")
                container.mount(widget)

    def load_form_page(self):
        """Load inputs for the current page."""
        form_container = self.query_one("#form", Vertical)
        form_container.remove_children()

        form_keys = list(self.form.keys())
        title = self.labelize(form_keys[self.current_page])
        current_dict = self.form[form_keys[self.current_page]]
        if self.current_page == self.len_pages - 1:
            current_dict = self.form
            title = "Other Settings"
        
        dict_id = form_keys[self.current_page] if (self.current_page != self.len_pages - 1) else ""
        self.gen_widgets(form_container, current_dict, dict_id=dict_id)

        self.query_one("#title", Static).update(f" [b]{title}[/b] {self.current_page + 1}/{self.len_pages}")

    def save_current_page_data(self):
        """Save the current page data to the form dictionary."""
        form_container = self.query_one("#form", Vertical)
        widgets = list(form_container.query(Input)) + list(form_container.query(Checkbox))
        for widget in widgets:
            widget_id = widget.id.split("__")
            form_value = self.form
            for part in widget_id:
                if part != "":
                    form_value = form_value[part]
            if widget.value is None or widget.value == "":
                continue
            elif form_value["data_type"] == "integer":
                form_value["data"] = int(widget.value)
            else :
                form_value["data"] = widget.value

    def clean_form_data(self, data):
        """Remove data_type fields and extract only the data values."""
        if isinstance(data, dict):
            if "data_type" in data and "data" in data:
                return data["data"]
            else:
                cleaned = {}
                for key, value in data.items():
                    cleaned[key] = self.clean_form_data(value)
                return cleaned
        return data

    def load_em_settings_from_file(self, config_file_path):
        """Load EM settings from a RAG config file."""
        if os.path.exists(config_file_path):
            try:
                with open(config_file_path, "r") as f:
                    rag_settings = json.load(f)
                # Load EM settings into the form
                em_settings = rag_settings.get("emSetting", {})
                if em_settings:
                    self.form["em_setting"]["api_key"]["type"]["data"] = "Raw"
                    self.form["em_setting"]["api_key"]["secret"]["data"] = em_settings.get("apiKey")
                    self.form["em_setting"]["api_base"]["data"] = em_settings.get("baseUrl")
                    self.form["em_setting"]["model"]["data"] = em_settings.get("model")
                    self.form["em_setting"]["provider"]["data"] = em_settings.get("provider")
                    # Update existing widgets if we're on the EM settings page
                    if self.current_page == 1:  # EM settings is the second page (index 1)
                        self.update_current_page_widgets()
            except (json.JSONDecodeError, KeyError) as e:
                # Handle file reading errors silently or show a message
                pass

    def update_current_page_widgets(self):
        """Update the values of widgets on the current page without recreating them."""
        form_container = self.query_one("#form", Vertical)
        
        # Update Input widgets
        for widget in form_container.query(Input):
            widget_id = widget.id.split("__")
            form_value = self.form
            for part in widget_id:
                if part != "":
                    form_value = form_value[part]
            if form_value.get("data") is not None:
                widget.value = str(form_value["data"])
        
        # Update Checkbox widgets
        for widget in form_container.query(Checkbox):
            widget_id = widget.id.split("__")
            form_value = self.form
            for part in widget_id:
                if part != "":
                    form_value = form_value[part]
            if form_value.get("data") is not None:
                widget.value = bool(form_value["data"])

    def get_em_config_file(self):
        """Open file browser specifically for EM settings config file."""
        class ConfigFileInput:
            def __init__(self, app):
                self.app = app
                self.value = None
            
            def __setattr__(self, name, value):
                if name == "value" and value:
                    self.app.load_em_settings_from_file(value)
                super().__setattr__(name, value)
        
        dummy_input = ConfigFileInput(self)
        file_browser = FileBrowserScreen(initial_path="./", target_input=dummy_input)
        self.push_screen(file_browser)

    def on_button_pressed(self, event: Button.Pressed) -> None:
        if event.button.id == "next":
            self.save_current_page_data()
            if self.current_page < self.len_pages - 1:
                self.current_page += 1
                self.load_form_page()
        elif event.button.id == "prev":
            self.save_current_page_data()
            if self.current_page > 0:
                self.current_page -= 1
                self.load_form_page()
        elif event.button.id == "save":
            self.save_current_page_data()
            # Clean and save the form data to a JSON file
            cleaned_data = self.clean_form_data(self.form)
            config_file = "config_file.json"
            i = 0
            while os.path.exists(config_file):
                i += 1
                config_file = f"config_file_{i}.json"
            
            with open(config_file, "w") as f:
                json.dump(cleaned_data, f, indent=2)
            exit(0)
        elif event.button.id.startswith("browse__"):
            # Handle browse button clicks for path inputs
            input_id = event.button.id.replace("browse__", "")
            input_widget = self.query_one(f"#{input_id}", Input)
            self.get_path_from_directory_tree_in_widget(input_widget)
        elif event.button.id == "load-em-settings":
            self.save_current_page_data()
            self.get_em_config_file()


if __name__ == "__main__":
    app = GenConfigApp()
    app.run()
