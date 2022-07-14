import {Control, DomEvent, DomUtil} from "leaflet";
import Layers = Control.Layers;

export default class LayersControl extends Layers {
    declare _layersLink: HTMLButtonElement;
    declare _container: HTMLDivElement;
    declare _baseLayersList: HTMLDivElement;
    declare _separator: HTMLDivElement;
    declare _overlaysList: HTMLDivElement;
    declare _section: HTMLElement;
    private expanded = false;

    // noinspection JSUnusedGlobalSymbols
    _initLayout() {
        // copied the contents of _initLayout() from leaflet-src (line 5055-5100)
        // now we can modify it to our needs without @ts-ignore \o/
        // i removed all the events, so we don't have to turn any off

        this._container = DomUtil.create('div', 'leaflet-control-layers');

        DomEvent.disableClickPropagation(this._container);
        DomEvent.disableScrollPropagation(this._container);

        this._section = DomUtil.create('section', 'leaflet-control-layers-list');

        this._layersLink =  DomUtil.create('button', 'leaflet-control-layers-toggle', this._container);
        this._layersLink.title = 'Layers';

        //Avoiding DomEvent here for more specific event typings
        this._layersLink.addEventListener('click', (e: MouseEvent) => {
            this.expanded ? this.collapse() : this.expand();
            e.preventDefault();
        });

        //Expand on right arrow press on button
        this._layersLink.addEventListener('keydown', (e: KeyboardEvent) => {
            if(e.key === 'ArrowRight') {
                this.expand();
                e.preventDefault();
            }
        });

        //Collapse on left arrow press on list
        this._section.addEventListener('keydown', (e: KeyboardEvent) => {
            if(e.key === 'ArrowLeft') {
                this.collapse();
                e.preventDefault();
            }
        });

        this._baseLayersList = DomUtil.create('div', 'leaflet-control-layers-base', this._section);
        this._separator = DomUtil.create('div', 'leaflet-control-layers-separator', this._section);
        this._overlaysList = DomUtil.create('div', 'leaflet-control-layers-overlays', this._section);

        this._container.appendChild(this._section);
    }

    expand() {
        this.expanded = true;
        this._layersLink.setAttribute('aria-expanded', 'true');

        super.expand();

        //Focus first layer checkbox
        const firstItem = this._section.querySelector('input');

        if(firstItem) {
            firstItem.focus();
        }

        return this;
    }

    collapse() {
        this.expanded = false;
        this._layersLink.removeAttribute('aria-expanded');
        this._layersLink.focus();

        return super.collapse();
    }
}
